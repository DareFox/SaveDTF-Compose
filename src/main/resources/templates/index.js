"use strict";
var isInDebugMode = true;
const modalMenu = document.querySelector(".modal");
const arrowLeft = document.querySelector(".modal-arrow-left");
const arrowRight = document.querySelector(".modal-arrow-right");
const closeButton = document.querySelector(".modal-exit-territory");
const divImages = document.querySelectorAll(".andropov_image");
const divGalleries = document.querySelectorAll(".gall");
const divCurrentElement = document.querySelector(".gl-current-element");
const galleryFooter = document.querySelector(".gl-footer");
const galleryFooterText = document.querySelector(".gl-counter");
var currentElement;
function registerAll() {
    divGalleries.forEach(gall => {
        registerGallery(gall);
    });
    divImages.forEach(img => {
        registerImage(img);
    });
}
function registerGallery(gallDiv) {
    if (!gallDiv)
        return;
    const galleryArray = [];
    const galleryElements = gallDiv.children;
    for (let element of galleryElements) {
        const rawPosition = element.getAttribute("pos");
        const url = element.getAttribute("media-url");
        const type = element.getAttribute("media-type");
        if (rawPosition != null && rawPosition.length > 0) {
            const numPosition = parseInt(rawPosition);
            const galleryElement = {
                position: numPosition,
                url: url,
                type: type,
                parent: galleryArray,
                element: element
            };
            galleryArray[numPosition] = galleryElement;
            element.addEventListener("click", () => {
                switchTo(galleryElement);
            });
        }
    }
}
function registerImage(image) {
    if (!image)
        return;
    var url;
    if (image instanceof HTMLDivElement) {
        var found = undefined;
        for (let element of image.children) {
            if (element instanceof HTMLImageElement) {
                found = element.src;
                break;
            }
        }
        if (!found) {
            log("Can't find link to this image:");
            log(image);
            found = "undefined";
        }
        url = found;
    }
    else if (image instanceof HTMLImageElement) {
        url = image.src;
    }
    else {
        log(`Unknown instance type ${image.tagName}`);
        log(image);
        return;
    }
    const imageElement = {
        url: url
    };
    image.addEventListener("click", (ev) => {
        switchTo(imageElement);
    });
}
function switchTo(element) {
    if (element == null) {
        closeModal();
        return;
    }
    if (isGalleryElement(element)) {
        openGalleryElement(element);
        return;
    }
    openImageElement(element);
}
function openGalleryElement(element) {
    log("Opening gallery element");
    var newElement;
    var url;
    if (element.url) {
        url = element.url;
    }
    else {
        url = "";
    }
    if (element.type === "video") {
        const video = document.createElement("video");
        const source = document.createElement("source");
        source.src = url;
        video.appendChild(source);
        video.autoplay = true;
        video.controls = true;
        video.muted = false;
        video.loop = true;
        newElement = video;
    }
    else {
        const img = document.createElement("img");
        img.src = url;
        newElement = img;
    }
    showArrows();
    setFooter(`${element.position + 1} из ${element.parent.length}`);
    setHtmlCurrentElement(newElement);
    currentElement = element;
}
function openImageElement(element) {
    log("Opening image element");
    const img = document.createElement("img");
    img.src = element.url;
    hideArrows();
    setFooter(null);
    setHtmlCurrentElement(img);
    currentElement = element;
}
function closeModal() {
    log("Closing modal");
    modalMenu === null || modalMenu === void 0 ? void 0 : modalMenu.classList.remove("modal--open");
    if (divCurrentElement)
        divCurrentElement.innerHTML = "";
}
function openModal() {
    log("Opening modal");
    modalMenu === null || modalMenu === void 0 ? void 0 : modalMenu.classList.add("modal--open");
}
function hideArrows() {
    log("Hiding buttons");
    if (arrowLeft)
        arrowLeft.style.display = "none";
    if (arrowRight)
        arrowRight.style.display = "none";
}
function showArrows() {
    log("Display buttons");
    if (arrowLeft)
        arrowLeft.style.display = "block";
    if (arrowRight)
        arrowRight.style.display = "block";
}
function setFooter(text) {
    if (text === null) {
        if (galleryFooter)
            galleryFooter.style.display = "none";
    }
    else {
        if (galleryFooter)
            galleryFooter.style.display = "block";
        if (galleryFooterText)
            galleryFooterText.innerHTML = text;
    }
}
function setHtmlCurrentElement(element) {
    if (divCurrentElement)
        divCurrentElement.innerHTML = "";
    if (element !== null) {
        if (divCurrentElement)
            divCurrentElement.appendChild(element);
    }
    openModal();
}
function keyboardListener(event) {
    log(`Got keyboard event w/ code ${event.code}`);
    if (event.key === "Escape")
        closeModal();
    if (event.key === "ArrowLeft")
        switchToPrevious();
    if (event.key === "ArrowRight")
        switchToNext();
}
function switchToNext() {
    log("Trying to switch to next element");
    if (!isGalleryElement(currentElement))
        return;
    const current = currentElement;
    var index = current.position + 1;
    var finished = false;
    while (!finished) {
        if (index == current.parent.length) {
            finished = true;
        }
        const element = current.parent[index];
        if (element !== undefined && element !== null) {
            log("Found next element");
            switchTo(element);
            finished = true;
        }
        else {
            index++;
        }
    }
}
function switchToPrevious() {
    log("Trying to switch to previous element");
    if (!isGalleryElement(currentElement))
        return;
    const current = currentElement;
    var index = current.position - 1;
    var finished = false;
    while (!finished) {
        if (index < 0) {
            finished = true;
        }
        const element = current.parent[index];
        if (element !== undefined && element !== null) {
            log("Found previous element");
            switchTo(element);
            finished = true;
        }
        else {
            index--;
        }
    }
}
document.onkeydown = keyboardListener;
arrowLeft === null || arrowLeft === void 0 ? void 0 : arrowLeft.addEventListener("click", (ev) => {
    log("Left arrow was pressed");
    switchToPrevious();
});
arrowRight === null || arrowRight === void 0 ? void 0 : arrowRight.addEventListener("click", (ev) => {
    log("Right arrow was pressed");
    switchToNext();
});
closeButton === null || closeButton === void 0 ? void 0 : closeButton.addEventListener("click", (ev) => {
    log("Close button was pressed");
    closeModal();
});
function log(message) {
    const date = new Date, formatedDate = [date.getMonth() + 1,
        date.getDate(),
        date.getFullYear()].join('/') + ' ' +
        [date.getHours(),
            date.getMinutes(),
            date.getSeconds()].join(':');
    const prefix = `[LOG - ${formatedDate}] `;
    if (isInDebugMode) {
        if (typeof message === 'string' || message == null) {
            console.log(prefix + message);
        }
        else {
            console.log(prefix);
            console.log(message);
        }
    }
}
function isGalleryElement(element) {
    if (element === null || element === undefined) {
        return false;
    }
    return "position" in element && "url" in element;
}
registerAll();
//# sourceMappingURL=newGallery.js.map