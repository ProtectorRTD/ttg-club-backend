package club.dnd5.portal.controller.api;

import club.dnd5.portal.dto.api.TokenValidationApi;
import club.dnd5.portal.dto.api.UserApi;
import club.dnd5.portal.dto.user.ChangePassword;
import club.dnd5.portal.dto.user.LoginDto;
import club.dnd5.portal.dto.user.SignUpDto;
import club.dnd5.portal.dto.user.UserDto;
import club.dnd5.portal.exception.PageNotFoundException;
import club.dnd5.portal.model.user.Role;
import club.dnd5.portal.model.user.User;
import club.dnd5.portal.repository.VerificationToken;
import club.dnd5.portal.repository.user.RoleRepository;
import club.dnd5.portal.repository.user.UserRepository;
import club.dnd5.portal.repository.user.VerificationTokenRepository;
import club.dnd5.portal.security.JWTAuthResponse;
import club.dnd5.portal.security.JwtTokenProvider;
import club.dnd5.portal.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.sql.Date;
import java.util.Calendar;
import java.util.Collections;
import java.util.Optional;

@RequiredArgsConstructor
@Tag(name = "User", description = "The User API")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthApiController {
	private final AuthenticationManager authenticationManager;
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final JwtTokenProvider tokenProvider;
	private final PasswordEncoder passwordEncoder;
	private final VerificationTokenRepository verificationTokenRepository;
	private final EmailService emailService;

	@Operation(summary = "User authorization by nickname or email address")
	@PostMapping("/signin")
	public ResponseEntity<JWTAuthResponse> authenticateUser(@RequestBody LoginDto loginDto, HttpServletResponse response) {
		try {
			Authentication authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(loginDto.getUsernameOrEmail(), loginDto.getPassword()));

			SecurityContextHolder.getContext().setAuthentication(authentication);

			String token = tokenProvider.generateToken(authentication);

			SecurityContextHolder.getContext().setAuthentication(authentication);

		    Cookie cookie = new Cookie("dnd5_user_token", token);
		    if (loginDto.getRemember()) {
			    cookie.setMaxAge(365 * 24 * 60 * 60);
		    }
		    else {
		    	cookie.setMaxAge(1 * 24 * 60 * 60);
		    }
			cookie.setPath("/");
		    response.addCookie(cookie);

			return ResponseEntity.ok(new JWTAuthResponse(token));
		}
		catch (BadCredentialsException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
	}

	@Operation(summary = "New user registration")
	@PostMapping("/signup")
	public ResponseEntity<?> registerUser(@RequestBody SignUpDto signUpDto) {

		if (userRepository.existsByUsername(signUpDto.getUsername())) {
			return new ResponseEntity<>("Username is already taken!", HttpStatus.BAD_REQUEST);
		}

		if (userRepository.existsByEmail(signUpDto.getEmail())) {
			return new ResponseEntity<>("Email is already taken!", HttpStatus.BAD_REQUEST);
		}

		User user = new User();
		user.setName(signUpDto.getUsername());
		user.setUsername(signUpDto.getUsername());
		user.setEmail(signUpDto.getEmail());
		user.setPassword(passwordEncoder.encode(signUpDto.getPassword()));
		user.setEnabled(Boolean.TRUE);

		Role roles = roleRepository.findByName("USER").get();
		user.setRoles(Collections.singletonList(roles));

		userRepository.save(user);
		return new ResponseEntity<>("User registered successfully", HttpStatus.OK);
	}

	@Operation(summary = "Log out user current session")
	@PostMapping("/signout")
	public ResponseEntity<?> signout(HttpSession session, HttpServletResponse response) {
	    Cookie cookie = new Cookie("dnd5_user_token", "");
	    cookie.setMaxAge(-1);
		cookie.setPath("/");
	    response.addCookie(cookie);
		session.invalidate();
		return ResponseEntity.ok().build();
	}

	@Operation(summary = "Nickname and mailing address check")
	@PostMapping("/exist")
	public ResponseEntity<?> isUserNotExist(@RequestBody UserDto user) {
		if (user.getUsername() != null) {
			if (userRepository.existsByUsername(user.getUsername())) {
				return ResponseEntity.status(HttpStatus.CONFLICT).build();
			}
		}
		if (user.getEmail() != null) {
			if (userRepository.existsByEmail(user.getEmail())) {
				return ResponseEntity.status(HttpStatus.CONFLICT).build();
			}
		}
		return ResponseEntity.ok().build();
	}

	@Operation(summary = "Change password by token")
	@PostMapping("/change/password")
	public ResponseEntity<?> changePassword(@RequestBody ChangePassword passwordDto) {
		if (passwordDto.getResetToken() != null) {
			VerificationToken token = verificationTokenRepository.findByToken(passwordDto.getResetToken()).orElseThrow(PageNotFoundException::new);
			User user = token.getUser();
			user.setPassword(passwordEncoder.encode(passwordDto.getPassword()));
			userRepository.save(user);
			token.setExpiryDate(VerificationToken.calculateExpiryDate(0));
			verificationTokenRepository.save(token);
			return ResponseEntity.ok().build();
		}
		else if (passwordDto.getUserToken() != null) {
			String userName = tokenProvider.getUsernameFromJWT(passwordDto.getUserToken());
			Optional<User> user = userRepository.findByEmailOrUsername(userName, userName);
			if (user.isPresent()) {
				User changeUser = user.get();
				changeUser.setPassword(passwordEncoder.encode(passwordDto.getPassword()));
				userRepository.save(changeUser);
				return ResponseEntity.status(HttpStatus.OK).build();
			}
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
	}

	@Operation(summary = "Send token for reset password by email")
	@GetMapping("/change/password")
	public ResponseEntity<UserApi> changeUserPassword(String email) {
		Optional<User> user = userRepository.findByEmail(email);
		emailService.changePassword(user.get());
		return ResponseEntity.ok().build();
	}

	@Operation(summary = "Check token exist")
	@GetMapping("/token/validate")
	public TokenValidationApi existToken(@RequestParam String token) {
		Optional<VerificationToken> verificationToken = verificationTokenRepository.findByToken(token);
		if (!verificationToken.isPresent()) {
			return new TokenValidationApi(false, "Неверный токен");
		}
		if (verificationToken.get().getExpiryDate().before(new Date(Calendar.getInstance().getTime().getTime()))){
			return new TokenValidationApi(false, "Время использование токена истекло!");
		}
		return new TokenValidationApi(true, "");
	}
}
