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

    $.ajax({
        type: 'GET',
        url: window.location.origin + '/getTasks',
        success: (result) => {
            for (const rowResult of result) {
                let rowHtml = document.querySelector('template').content.querySelector('tr').cloneNode(true);
                $(rowHtml).find('.id').text(rowResult.id);
                $(rowHtml).find('.src').text(rowResult.src);
                if (rowResult.creationDateTime) {
                    $(rowHtml).find('.creationDateTime').text(normalizeDate(rowResult.creationDateTime));
                }
                $(rowHtml).find('.client').text(rowResult.client);
                $(rowHtml).find('.creatorName').text(rowResult.creatorName);
                $(rowHtml).find('.executorName').text(rowResult.executorName);
                $(rowHtml).find('.description').text(rowResult.description);
                if (rowResult.lastChangedDateTime) {
                    $(rowHtml).find('.lastChangedDateTime').text(normalizeDate(rowResult.lastChangedDateTime));
                }
                $(rowHtml).find('.priority').text(rowResult.priority);
                $(rowHtml).find('.status').text(rowResult.status);
                $(rowHtml).addClass(priorityColor(rowResult.priority));
                if (rowResult.isDragged) {
                    $(rowHtml).addClass('isDragged');
                }
                if (rowResult.isAlmostExpired) {
                    $(rowHtml).addClass('almostExpired');
                }
                if (rowResult.priorityChanged) {
                    $(rowHtml).addClass('priorityChanged');
                }
                $('.container').append(rowHtml);
            }
        }
    });

    const table = $('.container').sortable({
        onUpdate: (evt) => {
            $(evt.item).addClass('isDragged');
            let rowList = $('.container .row').map((i, el) => {
                return {
                    compositeId: $(el).find('.id').text() + '-' + $(el).find('.src').text(),
                    priority: $(el).find('.priority').text(),
                    isDragged: $(el).hasClass('isDragged')
                }
            }).get();
            $.ajax({
                type: 'POST',
                url: window.location.origin + '/changeOrder',
                contentType: "application/json; charset=utf-8",
                dataType: "json",
                data: JSON.stringify(rowList)
            })
        }
    });
});
