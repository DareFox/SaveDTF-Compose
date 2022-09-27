declare var isInDebugMode: boolean;
interface GalleryElement {
    position: number;
    url: string | null;
    title: string | null;
    type: string | null;
    parent: GalleryElement[];
    element: Element;
}
interface ImageElement {
    url: string;
}
declare const modalMenu: Element | null;
declare const arrowLeft: HTMLElement | null;
declare const arrowRight: HTMLElement | null;
declare const closeButton: HTMLElement | null;
declare const divImages: NodeListOf<HTMLElement>;
declare const divGalleries: NodeListOf<Element>;
declare const divCurrentElement: Element | null;
declare const galleryFooter: HTMLElement | null;
declare const galleryFooterTitle: HTMLElement | null;
declare const galleryFooterCounter: Element | null;
declare var currentElement: GalleryElement | ImageElement | null;
declare function registerAll(): void;
declare function registerGallery(gallDiv: Element): void;
declare function registerImage(image: Element): void;
declare function switchTo(element: GalleryElement | ImageElement | null): void;
declare function openGalleryElement(element: GalleryElement): void;
declare function openImageElement(element: ImageElement): void;
declare function setFooterDescription(desc: string | null): void;
declare function closeModal(): void;
declare function openModal(): void;
declare function hideArrows(): void;
declare function showArrows(): void;
declare function setFooterCounter(text: string | null): void;
declare function setHtmlCurrentElement(element: Element | null): void;
declare function keyboardListener(event: KeyboardEvent): void;
declare function switchToNext(): void;
declare function switchToPrevious(): void;
declare function log(message: string | null | any): void;
declare function isGalleryElement(element: any | null): Boolean;
