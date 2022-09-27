/**
 * Hide button behaviour
 */
declare const hides: NodeListOf<Element>;
declare function hideNodes(nodeWrapper: Element, commentWrapper: Element): void;
/**
 * Date formating
 */
declare const dates: NodeListOf<Element>;
declare function formatDate(date: Date): string;
/**
 * "Reply to" behaviour
 */
declare const comments: NodeListOf<Element>;
declare const allReplyToAnimations: string[];
declare function onHoverEnter(parent: Element): void;
declare function onHoverExit(parent: Element): void;
declare function onHoverClick(parent: Element): void;
