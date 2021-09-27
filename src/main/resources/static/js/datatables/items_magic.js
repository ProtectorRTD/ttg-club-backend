$(document).ready(function() {
	var table = $('#items_magic').DataTable({
		ajax : '/data/items/magic',
		dom: 'tiS',
		serverSide : true,
        deferRender: true,
        scrollY: "850px",
        scrollCollapse: true,
        scroller: true,
		select: true,
		select: {
			style: 'single'
		},
		columns : [
		{
			data : 'rarity',
		},
		{
			data : "name",
			render : function(data, type, row) {
				if (type === 'display') {
					var result ='<div class="spell_lvl">' + row.shortRarity + '</div>';
					result+='<div class="spell_name">' + row.name;
					result+='<span>' + row.englishName + '</span></div>';
					result+='<div class="spell_school">' + row.type + '</div>';
					return result;
				}
				return data;
			}
		}, 
		{
			data : 'englishName',
		},
		],
		columnDefs : [
			{
				"targets": [ 0 ],
				"visible": false
			},
			{
				"targets": [ 2 ],
				"visible": false
			},
		],
		order : [[0, 'asc']],
		language : {
			processing : "Загрузка...",
			searchPlaceholder: "Поиск ",
			search : "_INPUT_",
			lengthMenu : "Показывать _MENU_ записей на странице",
			zeroRecords : "Ничего не найдено",
			info : "Показано _TOTAL_",
			infoEmpty : "Нет доступных записей",
			infoFiltered : "из _MAX_",
		},
		initComplete: function(settings, json) {
		    $('#items_magic tbody tr:eq(0)').click();
		    table.row(':eq(0)', { page: 'current' }).select(); 
		}
	});

	$('#items_magic tbody').on('click', 'tr', function () {
		var tr = $(this).closest('tr');
		var table = $('#items_magic').DataTable();
		var row = table.row( tr );
		var data = row.data();
		document.getElementById('item_name').innerHTML = data.name;
		document.getElementById('type').innerHTML = data.type;
		document.getElementById('rarity').innerHTML = data.rarity;
		document.getElementById('attunement').innerHTML = data.attunement;
		document.getElementById('cost').innerHTML = data.cost;

		var source = '<span class="tip" data-tipped-options="inline: \'inline-tooltip-source-' +data.id+'\'">' + data.bookshort + '</span>';
		source+= '<span id="inline-tooltip-source-'+ data.id + '" style="display: none">' + data.book + '</span>';
		document.getElementById('source').innerHTML = source;

		history.pushState('data to be passed', '', '/items/magic/' + data.englishName.split(' ').join('_'));
		var url = '/items/magic/fragment/' + data.id;
		$(".content_block").load(url);
	});
	$('#search').on( 'keyup click', function () {
		table.tables().search($(this).val()).draw();
	});
});