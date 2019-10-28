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

    const toggleSortable = (executorName, status) => {
        let isDisabled = !(executorName === 'Any' && status === 'Any');
        $('.container').sortable('disabled', isDisabled);
    };

    const getFilteredList = (frontList) => {
        let executorName = $('.executor').val();
        let status = $('.status').val();

        toggleSortable(executorName, status);

        return $(frontList).filter((i, el) => {
            if (executorName === 'Any') {
                return true;
            } else {
                return $(el).find('.executorName').text() === executorName;
            }
        }).filter((i, el) => {
            if (status === 'Any') {
                return true;
            } else {
                return $(el).find('.status').text() === status;
            }
        })
    };

    const drawList = (frontList) => {
        $(frontList).each((i, el) => $('.container').append($(el)));
    };

    let frontList = [];

    $.ajax({
        type: 'GET',
        url: window.location.origin + '/getTasks',
        success: (rowList) => {
            if (rowList === null) {
                throw new Error('500: database error')
            }
            const distinctExecutorNames = sortedDistinct(rowList.map(row => row.executorName));
            for (const option of distinctExecutorNames) {
                let executorOpt = document.querySelector('.executor template').content.querySelector('option').cloneNode(true);
                executorOpt.value = option;
                executorOpt.textContent = option;
                $('.executor').append(executorOpt);
            }

            const distinctStatuses = sortedDistinct(rowList.map(row => row.status));
            for (const option of distinctStatuses) {
                let statusOpt = document.querySelector('.status template').content.querySelector('option').cloneNode(true);
                statusOpt.value = option;
                statusOpt.textContent = option;
                $('.status').append(statusOpt);
            }

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

            frontList = $('.container .row');

            $('.select').change(() => {
                $('.container > .row').remove();
                drawList(getFilteredList(frontList));
            });
        }
    });

    $('.container').sortable({
        onUpdate: (evt) => {
            $(evt.item).addClass('isDragged');
            frontList = $('.container .row');
            let rowList = frontList.map((i, el) => {
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
