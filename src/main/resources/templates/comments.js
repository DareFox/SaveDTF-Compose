"use strict";
const hides = document.querySelectorAll('.hide:not(.hide--invisible):not(.hide--disabled)');
hides.forEach(hide => {
    hide.addEventListener("click", (_ => {
        var _a, _b;
        const nodeWrapper = (_b = (_a = hide.parentElement) === null || _a === void 0 ? void 0 : _a.parentElement) === null || _b === void 0 ? void 0 : _b.closest(".nodes-wrapper");
        if (!nodeWrapper)
            return;
        const commentWrapper = nodeWrapper.parentElement.querySelector(".comment-wrapper");
        if (!commentWrapper)
            return;
        hideNodes(nodeWrapper, commentWrapper);
    }));
});
const dates = document.querySelectorAll(".comment .info .date");


dates.forEach(date => {
    const unix = date.getAttribute("unix-time");
    if (!unix)
        return;
    date.innerHTML = formatDate(new Date(Number(unix) * 1000));
});

function formatDate(date) {
    let dayOfMonth = date.getDate();
    let month = date.getMonth() + 1;
    let year = date.getFullYear();
    let hour = date.getHours();
    let minutes = date.getMinutes();
    let diffMs = new Date().getTime() - date.getTime();
    let diffSec = Math.round(diffMs / 1000);
    let diffMin = Math.round(diffSec / 60);
    let diffHour = Math.round(diffMin / 60);
    // форматирование
    let newyear = year.toString().slice(-2);
    let newmonth = month < 10 ? '0' + month : month;
    let newdayOfMonth = dayOfMonth < 10 ? '0' + dayOfMonth : dayOfMonth;
    let newhour = hour < 10 ? '0' + hour : hour;
    let newminutes = minutes < 10 ? '0' + minutes : minutes;
    if (diffSec < 1) {
        return 'прямо сейчас';
    }
    else if (diffMin < 1) {
        return `${diffSec} сек. назад`;
    }
    else if (diffHour < 1) {
        return `${diffMin} мин. назад`;
    }
    else {
        return `${newdayOfMonth}.${newmonth}.${newyear} ${newhour}:${newminutes}`;
    }
}
function hideNodes(nodeWrapper, commentWrapper) {
    const footer = commentWrapper.querySelector(".comment .footer");
    if (!footer)
        return;
    let openCommentsButton = footer.querySelector("a.openComments");
    // if no button, create it
    if (!openCommentsButton) {
        openCommentsButton = document.createElement("a");
        openCommentsButton.classList.add("openComments");
        footer.appendChild(openCommentsButton);
    }
    // hide nodes
    nodeWrapper.setAttribute("style", "display: none");
    // make reset button visable
    openCommentsButton.setAttribute("style", "");
    openCommentsButton.innerHTML = "Показать скрытые комментарии";
    // button to re-open
    openCommentsButton.addEventListener("click", () => {
        nodeWrapper.setAttribute("style", "");
        openCommentsButton === null || openCommentsButton === void 0 ? void 0 : openCommentsButton.setAttribute("style", "display: none");
    });
}
