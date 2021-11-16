$(document).ready(function() {
	var scrollEventHeight = 0;
	var rowSelectIndex = 0;
	var table = $('#items').DataTable({
		ajax : '/data/items',
		dom: 'tiS',
		serverSide : true,
        deferRender: true,
		iDisplayLength : 30,
        scrollCollapse: true,
		select: true,
		select: {
			style: 'single'
		},
        searchPanes: {
            initCollapsed: true,
            viewCount: false,
            dtOpts: {
                select: {
                    //style: 'multi'
                },
				searching: false,
            },
			orderable: false
        },
		columns : [
		{
			data : "name",
			render : function(data, type, row) {
				if (type === 'display') {
					var result ='<div class="spell_name">' + row.name;
					result+='<span>' + row.englishName + '</span></div>';
					return result;
				}
				return data;
			}
		}, 
		{
			data : 'englishName',
		},
		{
			data : 'type',
			searchable: false,
		},
		],
		columnDefs : [
			{
				"targets": [ 1, 2 ],
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
			loadingRecords: "Загрузка...",
	        searchPanes: {
	            title: {
	                 _: 'Выбрано фильтров - %d',
	                 0: 'Фильтры не выбраны',
	                 1: 'Один фильтр выбран'
	            },
                collapseMessage: 'Свернуть все',
                showMessage: 'Развернуть все',
                clearMessage: 'Сбросить фильтры'
	        }
		},
		initComplete: function(settings, json) {
			scrollEventHeight = document.getElementById('scroll_load_simplebar').offsetHeight - 400;
		    const simpleBar = new SimpleBar(document.getElementById('scroll_load_simplebar'));
		    simpleBar.getScrollElement().addEventListener('scroll', function(event){
		    	if (simpleBar.getScrollElement().scrollTop > scrollEventHeight){
		    	      table.page.loadMore();
		    	      simpleBar.recalculate();
		    	      scrollEventHeight +=750;
		    	}
		    });
		    table.searchPanes.container().prependTo($('#searchPanes'));
		    table.searchPanes.container().hide();
		},
		drawCallback: function ( settings ) {
			if(rowSelectIndex === 0 && selectedItem === null){
				if (!$('#list_page_two_block').hasClass('block_information')){
					return;
				}
				$('#items tbody tr:eq('+rowSelectIndex+')').click();
			}
			if (selectedItem) {
				selectItem(selectedItem);
				var rowIndexes = [];
				table.rows( function ( idx, data, node ) {
					if(data.id === selectedItem.id){
						rowIndexes.push(idx);
					}
					return false;
				});
				rowSelectIndex = rowIndexes[0];
			}
			table.row(':eq('+rowSelectIndex+')', { page: 'current' }).select();
		}
	});
	$('#items tbody').on('click', 'tr', function () {
		if(!document.getElementById('list_page_two_block').classList.contains('block_information')){
			document.getElementById('list_page_two_block').classList.add('block_information');
		}
		var tr = $(this).closest('tr');
		var table = $('#items').DataTable();
		var row = table.row( tr );
		var data = row.data();
		rowSelectIndex = row.index();
		selectItem(data);
		selectedItem = null;
	});
	$('#search').on( 'keyup click', function () {
		table.tables().search($(this).val()).draw();
	});
	$('#btn_filters').on('click', function() {
		var table = $('#items').DataTable();
		table.searchPanes.container().toggle();
	});
});
function selectItem(data){
	document.getElementById('item_name').innerHTML = data.name;
	document.getElementById('type').innerHTML = data.type;
	document.getElementById('cost').innerHTML = data.cost;
	document.getElementById('weight').innerHTML = data.weight;
	var source = '<span class="tip" data-tipped-options="inline: \'inline-tooltip-source-' +data.id+ '\'">' + data.bookshort + '</span>';
	source+= '<span id="inline-tooltip-source-'+ data.id + '" style="display: none">' + data.book + '</span>';
	document.getElementById('source').innerHTML = source;
	document.title = data.name;
	history.pushState('data to be passed', '', '/items/' + data.englishName.split(' ').join('_'));
	var url = '/items/fragment/' + data.id;
	$("#content_block").load(url);	
}
$('#btn_close').on('click', function() {
	document.getElementById('list_page_two_block').classList.remove('block_information');
});