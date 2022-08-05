// TO SEE LOGS, SET THIS VARIABLE TO TRUE
var isInDebugMode = true;

interface GalleryElement {
    position: number,
    url: string | null,
    type: string | null,
    parent: GalleryElement[],
    element: Element
} 

interface ImageElement {
    url: string
}

const modalMenu = document.querySelector(".modal");

const arrowLeft = document.querySelector<HTMLElement>(".modal-arrow-left");
const arrowRight = document.querySelector<HTMLElement>(".modal-arrow-right");
const closeButton = document.querySelector<HTMLElement>(".modal-exit-territory");

const divImages = document.querySelectorAll<HTMLElement>(".andropov_image");
const divGalleries = document.querySelectorAll(".gall");
const divCurrentElement = document.querySelector(".gl-current-element");

const galleryFooter = document.querySelector<HTMLElement>(".gl-footer");
const galleryFooterText = document.querySelector(".gl-counter");

var currentElement: GalleryElement | ImageElement | null

function registerAll() {
    divGalleries.forEach(gall => {
        registerGallery(gall)
    })

    divImages.forEach(img => {
        registerImage(img)
    })
}

function registerGallery(gallDiv: Element) {
    if (!gallDiv) return 
    
    const galleryArray: GalleryElement[] = []
    const galleryElements = gallDiv.querySelectorAll(".gall--item")
    
    for (let element of galleryElements) {
        const rawPosition = element.getAttribute("pos")
        const url = element.getAttribute("media-url")
        const type = element.getAttribute("media-type")

        if (rawPosition != null && rawPosition.length > 0) {
            const numPosition = parseInt(rawPosition)

            const galleryElement = {
                position: numPosition,
                url: url,
                type: type, 
                parent: galleryArray,
                element: element
            }

            galleryArray[numPosition] = galleryElement
            element.addEventListener("click", () => {
                switchTo(galleryElement)
            })
        }
    }
}

function registerImage(image: Element) {
    if (!image) return 

    var url: string 

    // Maybe there's more simplier approach to this
    if (image instanceof HTMLDivElement) {
        var found: string | undefined = undefined
        
        // Get first img and use url from it 
        for (let element of image.children) {
            if (element instanceof HTMLImageElement) {
                found = element.src
                break
            }
        }

        if (!found) {
            log("Can't find link to this image:")
            log(image)
            found = "undefined"
        }

        url = found
    } else if (image instanceof HTMLImageElement) {
        url = image.src
    } else {
        log(`Unknown instance type ${image.tagName}`)
        log(image)
        return
    }

    const imageElement: ImageElement = {
        url: url
    }
    
    image.addEventListener("click", (ev) => {
        switchTo(imageElement)
    })
}

function switchTo(element: GalleryElement | ImageElement | null) {
    if (element == null) {
        closeModal()
        return
    }
    
    if (isGalleryElement(element)) { // if GalleryElement
        openGalleryElement(element as GalleryElement)
        return
    }

    // else it's ImageElement
    openImageElement(element as ImageElement)
}

 
function openGalleryElement(element: GalleryElement) {
    log("Opening gallery element")
    var newElement: Element
    var url: string

    if (element.url) { 
        url = element.url 
    } else { 
        url = ""
    }


    if (element.type === "video") {
        const video = document.createElement("video")
        const source = document.createElement("source")

        source.src = url
        
        video.appendChild(source)
        video.autoplay = true
        video.controls = true
        video.muted = false
        video.loop = true

        newElement = video
    } else {
        const img = document.createElement("img")
        img.src = url

        newElement = img
    }

    showArrows()
    setFooter(`${element.position + 1} из ${element.parent.length}`)
    setHtmlCurrentElement(newElement) // open modal and show new element
    currentElement = element
}

function openImageElement(element: ImageElement) {
    log("Opening image element")
    const img = document.createElement("img")
    img.src = element.url

    hideArrows()
    setFooter(null) // remove footer
    setHtmlCurrentElement(img) // open modal and show new element
    currentElement = element

}

/*
    Modal menu functions
*/

function closeModal() {
    log("Closing modal")
    modalMenu?.classList.remove("modal--open")
    
    // Remove all inner html, because hiding <video> element doesn't unload it
    if (divCurrentElement) divCurrentElement.innerHTML = ""


}

function openModal() {
    log("Opening modal")
    modalMenu?.classList.add("modal--open")
}

function hideArrows() {
    log("Hiding buttons")
    if (arrowLeft) arrowLeft.style.display = "none"
    if (arrowRight) arrowRight.style.display = "none"
}

function showArrows() {
    log("Display buttons")
    if (arrowLeft) arrowLeft.style.display = "block"
    if (arrowRight) arrowRight.style.display = "block"
}

function setFooter(text: string | null) {
    if (text === null) {
        if (galleryFooter) galleryFooter.style.display = "none"
    } else {
        if (galleryFooter) galleryFooter.style.display = "block"
        if (galleryFooterText) galleryFooterText.innerHTML = text
    }
}

function setHtmlCurrentElement(element: Element | null) {
    if (divCurrentElement) divCurrentElement.innerHTML = ""
    
    if (element !== null) {
        if (divCurrentElement) divCurrentElement.appendChild(element)    
    }
    openModal()
}

/*
    Keyboard events handler
*/
function keyboardListener(event: KeyboardEvent) {
    log(`Got keyboard event w/ code ${event.code}`)
    if (event.key === "Escape") closeModal()
    if (event.key === "ArrowLeft") switchToPrevious()
    if (event.key === "ArrowRight") switchToNext()
}

function switchToNext() {
    log("Trying to switch to next element")

    if (!isGalleryElement(currentElement)) return
    const current = currentElement as GalleryElement
    
    var index = current.position + 1
    var finished = false

    while (!finished) {
        if (index == current.parent.length) {
            finished = true
        }

        const element: GalleryElement | undefined = current.parent[index] 

        if (element !== undefined && element !== null) {
            log("Found next element")
            switchTo(element as GalleryElement)
            finished = true
        } else {
            index++
        }
    }
}

function switchToPrevious() {
    log("Trying to switch to previous element")

    if (!isGalleryElement(currentElement)) return
    const current = currentElement as GalleryElement
    
    var index = current.position - 1
    var finished = false

    while (!finished) {
        if (index < 0) {
            finished = true
        }

        const element: GalleryElement | undefined = current.parent[index] 

        if (element !== undefined && element !== null) {
            log("Found previous element")
            switchTo(element as GalleryElement)
            finished = true
        } else {
            index--
        }
    }
}

document.onkeydown = keyboardListener


/*
    On screen buttons event handlers
 */
arrowLeft?.addEventListener("click", (ev) => {
    log("Left arrow was pressed")
    switchToPrevious()
})

arrowRight?.addEventListener("click", (ev) => {
    log("Right arrow was pressed")
    switchToNext()
})

closeButton?.addEventListener("click", (ev) => {
    log("Close button was pressed")
    closeModal()
})


/*
    Utilitty functions
 */
function log(message: string | null | any) {
    const date = new Date,
    formatedDate = [date.getMonth()+1,
               date.getDate(),
               date.getFullYear()].join('/')+' '+
              [date.getHours(),
               date.getMinutes(),
               date.getSeconds()].join(':');


    const prefix = `[LOG - ${formatedDate}] `
    
    if (isInDebugMode) {
        if (typeof message === 'string' || message == null) {
            console.log(prefix + message)
        } else {
            console.log(prefix) // don't convert message object to string
            console.log(message) // for better output in console
        }
    }
}

function isGalleryElement(element: any | null): Boolean {
    if (element === null || element === undefined) {
        return false
    }

    return "position" in element && "url" in element
}

/*
    Script start
*/
registerAll()
