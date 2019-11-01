'use strict';

$(() => {
    const normalizeDate = ((str) => {
        return str.substring(0, str.indexOf('.')).replace('T', ' ');
    });

    const priorityColor = ((p) => {
        let color;
        switch (p) {
            case 1:
                color = 'p1';
                break;
            case 2:
                color = 'p2';
                break;
            case 3:
                color = 'p3';
                break;
            case 4:
                color = 'p4';
                break;
        }
        return color;
    });

    const sortedDistinct = (array) => {
        return [...new Set(array)].sort();
    };

    const drawList = (list) => {
        $(list).each((i, el) => $('.container').append($(el)));
    };

    $.ajax({
        type: 'GET',
        url: window.location.origin + `/getData?executorName=${$('.executorName-filter').val()}&status=${$('.status-filter').val()}`,
        success: (rowList) => {
            for (const rowFromBack of rowList) {
                let rowHtml = document.querySelector('.container template').content.querySelector('tr').cloneNode(true);
                $(rowHtml).find('.id').text(rowFromBack.id);
                $(rowHtml).find('.src').text(rowFromBack.src);
                if (rowFromBack.creationDateTime) {
                    $(rowHtml).find('.creationDateTime').text(normalizeDate(rowFromBack.creationDateTime));
                }
                $(rowHtml).find('.client').text(rowFromBack.client);
                $(rowHtml).find('.creatorName').text(rowFromBack.creatorName);
                $(rowHtml).find('.executorName').text(rowFromBack.executorName);
                $(rowHtml).find('.description').text(rowFromBack.description);
                if (rowFromBack.lastChangedDateTime) {
                    $(rowHtml).find('.lastChangedDateTime').text(normalizeDate(rowFromBack.lastChangedDateTime));
                }
                $(rowHtml).find('.priority').text(rowFromBack.priority);
                $(rowHtml).find('.status').text(rowFromBack.status);
                $(rowHtml).addClass(priorityColor(rowFromBack.priority));
                if (rowFromBack.isDragged) {
                    $(rowHtml).addClass('isDragged');
                }
                if (rowFromBack.isAlmostExpired) {
                    $(rowHtml).addClass('almostExpired');
                }
                if (rowFromBack.priorityChanged) {
                    $(rowHtml).find('.priority').addClass('priorityChanged');
                }
                $('.container').append(rowHtml);
            }
        }
    });

    $('.container').sortable({
        onUpdate: (evt) => {
            $(evt.item).addClass('isDragged');
            let rowList = $('.container .row').map((i, el) => {
                return {
                    compositeId: $(el).find('.id').text() + '-' + $(el).find('.src').text(),
                    priority: $(el).find('.priority').text(),
                    isDragged: $(el).hasClass('isDragged'),
                    position: i,
                    executorName: $('.executorName-filter').val(),
                    status: $('.status-filter').val()
                }
            }).get();
            $.ajax({
                type: 'POST',
                url: window.location.origin + `/changeOrder?executorName=${$('.executorName-filter').val()}&status=${$('.status-filter').val()}`,
                contentType: "application/json; charset=utf-8",
                dataType: "json",
                data: JSON.stringify(rowList)
            })
        }
    });

    $('.select').change(() => {
        //todo ajax to get filtered list


        $('.container > .row').remove();

    });

    //todo прямые ссылки на заявки
    //todo количество заявок в select
    //todo возможность двигать заявки с сохранением с любыми фильтрами
});
