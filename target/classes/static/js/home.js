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

    const getDataAndDrawPage = (executorNameFilter, statusFilter) => {
        $.ajax({
            type: 'GET',
            url: window.location.origin + `/getData?executorName=${executorNameFilter}&status=${statusFilter}`,
            success: (data) => {
                for (const option of data.executorOptList) {
                    let executorOpt = document.querySelector('.executorName-filter template').content.querySelector('option').cloneNode(true);
                    executorOpt.value = option.substring(0, option.indexOf('(') - 1);
                    executorOpt.textContent = option;
                    $('.executorName-filter').append(executorOpt);
                }
                for (const option of data.statusOptList) {
                    let statusOpt = document.querySelector('.executorName-filter template').content.querySelector('option').cloneNode(true);
                    statusOpt.value = option.substring(0, option.indexOf('(') - 1);
                    statusOpt.textContent = option;
                    $('.status-filter').append(statusOpt);
                }

                for (const rowFromBack of data.resultList) {
                    let rowHtml = document.querySelector('.container template').content.querySelector('tr').cloneNode(true);
                    $(rowHtml).find('.id a').attr('href', rowFromBack.href);
                    $(rowHtml).find('.id a').text(rowFromBack.id);
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
                    if (rowFromBack.lastComment) {
                        rowHtml.querySelector('.lastComment').innerHTML = rowFromBack.lastComment.replace('pre>', 'p>');
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
                $('.executorName-filter').val(executorNameFilter);
                $('.status-filter').val(statusFilter);
            }
        });
    };

    getDataAndDrawPage('Любой', 'Любой');

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

    $('select').change(() => {
        let executorNameFilter = $('.executorName-filter').val();
        let statusFilter = $('.status-filter').val();
        $('.container > .row').remove();
        $('select .varOpt').remove();
        getDataAndDrawPage(executorNameFilter, statusFilter);
    });
});
