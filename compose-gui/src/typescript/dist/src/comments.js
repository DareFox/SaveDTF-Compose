"use strict";
/**
 * Hide button behaviour
 */
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
/**
 * Date formating
 */
const dates = document.querySelectorAll(".comment .info .date");
dates.forEach(date => {
    const unix = date.getAttribute("unix-time");
    if (!unix)
        return;
    date.innerHTML = formatDate(new Date(Number(unix) * 1000));
});
// 
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
    // format values
    let formatedYear = year.toString().slice(-2);
    let fomratedMonth = month < 10 ? '0' + month : month;
    let formatedDay = dayOfMonth < 10 ? '0' + dayOfMonth : dayOfMonth;
    let formatedHour = hour < 10 ? '0' + hour : hour;
    let formatedMinutes = minutes < 10 ? '0' + minutes : minutes;
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
        return `${formatedDay}.${fomratedMonth}.${formatedYear} ${formatedHour}:${formatedMinutes}`;
    }
}
/**
 * "Reply to" behaviour
 */
const comments = document.querySelectorAll(".comments-list .comment-node");
const allReplyToAnimations = ["replyToFastEnter", "replyToFastExit", "replyToSlowExit"];
comments.forEach(comment => {
    var _a;
    const replyToDiv = comment.querySelector(".comment .header .replyTo");
    if (!replyToDiv)
        return;
    const nodewrap = comment.closest(".nodes-wrapper");
    const commentWrapper = (_a = nodewrap === null || nodewrap === void 0 ? void 0 : nodewrap.parentElement) === null || _a === void 0 ? void 0 : _a.querySelector(".comment-wrapper");
    if (!commentWrapper)
        return;
    let clickDate = new Date();
    replyToDiv.innerHTML = "↑";
    replyToDiv.addEventListener("mouseover", () => {
        onHoverEnter(commentWrapper);
    });
    replyToDiv.addEventListener("mouseout", () => {
        if (new Date().getTime() - clickDate.getTime() > 100) {
            onHoverExit(commentWrapper);
        }
    });
    replyToDiv.addEventListener("click", () => {
        clickDate = new Date();
        onHoverClick(commentWrapper);
    });
});
function onHoverEnter(parent) {
    parent.classList.remove(...allReplyToAnimations);
    parent.classList.add("replyToFastEnter");
}
function onHoverExit(parent) {
    parent.classList.remove(...allReplyToAnimations);
    parent.classList.add("replyToFastExit");
}
function onHoverClick(parent) {
    parent.classList.remove(...allReplyToAnimations);
    parent.classList.add("replyToSlowExit");
    parent.scrollIntoView({
        behavior: "smooth",
        block: "center",
    });
}
