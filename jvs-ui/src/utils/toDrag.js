// fast create dom with id
function getComputedStyleList(el, names, formatToNumber = false) {
    const results = {};
    const styles = window.getComputedStyle(el);
    names.map(name => {
        let result = styles[name];
        if (formatToNumber) {
            const match = result.match(/\d+/);
            if (match)
                result = ~~match[0];
        }
        results[name] = result;
    });
    return results;
}

class ToDrag {
    constructor({ el, options }) {
        this.left = 0;
        this.top = 0;
        this.right = 0;
        this.bottom = 0;
        this.width = 0;
        this.height = 0;
        this.maxX = 0;
        this.maxY = 0;
        this.startX = 0;
        this.startY = 0;
        this.getScrollbarWidth = () => {
            const el = document.createElement('div');
            el.style.cssText = 'width:100px;height:100px;overflow-y:scroll';
            document.body.appendChild(el);
            const scrollbarWidth = el.offsetWidth - el.clientWidth;
            document.body.removeChild(el);
            return scrollbarWidth;
        };
        this.setBetween = (num, min, max) => {
            if (num < min)
                return min;
            if (num > max)
                return max;
            return num;
        };
        // absolute limit add border, padding size control
        this.borderInfo = [0, 0, 0, 0]; // [top, right, bottom, left]
        this.el = el instanceof HTMLElement ? el : document.querySelector(el);
        this.scrollbarWidth = this.getScrollbarWidth();
        this.isTouch = 'ontouchstart' in document.documentElement;
        this.isDrag = false;
        this.options = Object.assign({ moveCursor: true, adsorb: 0, adsorbOffset: 0, transitionDuration: 400, transitionTimingFunction: 'ease-in-out', forbidBodyScroll: true, isAbsolute: false, positionMode: 1, needComputeBorder: true }, options);
        this.parent = (this.options.parentSelector && document.querySelector(this.options.parentSelector)) || this.el.parentNode;
        if (this.options.transitionDuration) {
            this.options.transitionDuration = this.options.transitionDuration / 1000;
        }
        // init
        this.handleTouchStart = this.handleTouchStart.bind(this);
        this.handleMousedown = this.handleMousedown.bind(this);
        this.moveEvent = this.moveEvent.bind(this);
        this.endEvent = this.endEvent.bind(this);
        this.init();
    }
    init() {
        if (this.isTouch) {
            this.el.addEventListener('touchstart', this.handleTouchStart);
        }
        else {
            this.el.addEventListener('mousedown', this.handleMousedown);
        }
        if (this.options.moveCursor) {
            this.el.style.cursor = 'move';
        }
        this.setPosition();
        this.setLimit();
        this.handleAdsorb();
        this.handlePositionMode();
        setTimeout(() => {
            this.emitEvent('todraginit');
        });
    }
    handleMousedown(e) {
        if (typeof this.options.disabled === 'function' && this.options.disabled()) {
            return;
        }
        if(this.options.position===2){
            const { x, y } = e;
            this.setStartInfo(x, y);
        }else{
            const { pageX, pageY } = e;
            this.setStartInfo(pageX, pageY);
        }
        document.addEventListener('mousemove', this.moveEvent);
        document.addEventListener('mouseup', this.endEvent);
    }
    handleTouchStart(e) {
        if (typeof this.options.disabled === 'function' && this.options.disabled()) {
            return;
        }
        const x = this.isTouch ? e.changedTouches[0].clientX : e.x;
        const y = this.isTouch ? e.changedTouches[0].clientY : e.y;
        this.setStartInfo(x, y);
        document.addEventListener('touchmove', this.moveEvent, { passive: false });
        document.addEventListener('touchend', this.endEvent);
    }
    setPosition() {
        const { left, top, width, height } = this.el.getBoundingClientRect();
        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;
        if (!this.options.isAbsolute) {
            this.maxX = document.body.scrollWidth > window.innerWidth ? window.innerWidth - this.width - this.scrollbarWidth : window.innerWidth - this.width;
            this.maxY = document.body.scrollHeight > window.innerHeight ? window.innerHeight - this.height - this.scrollbarWidth : window.innerHeight - this.height;
        }
        else {
            // let parentDom = document.getElementById(this.options.ids)
            // this.maxX = parentDom.offsetWidth - this.width;
            // this.maxY = parentDom.offsetHeight - this.height;
            this.maxX = this.parent.offsetWidth - this.width;
            this.maxY = this.parent.offsetHeight - this.height;
        }
    }
    setStartInfo(x, y) {
        this.setPosition();
        this.setLimit();
        this.startX = x - this.left;
        this.startY = y - this.top;
        this.isDrag = true;
        this.el.style.transition = '';
        document.body.style.userSelect = 'none';
        if (this.options.forbidBodyScroll && this.options.position!=2) {
            document.body.style.overflow = 'hidden';
        }
        this.emitEvent('todragstart');
    }
    moveEvent(e) {
        if (!this.isDrag) {
            return;
        }
        e.preventDefault();
        let dragX, dragY;
        const x = this.isTouch ? e.changedTouches[0].clientX : e.x;
        const y = this.isTouch ? e.changedTouches[0].clientY : e.y;
        if (!this.options.isAbsolute) {
            dragX = x - this.startX;
            dragY = y - this.startY;
        }
        else {
            const parentDomRect = this.parent.getClientRects()[0]
            dragX = x - parentDomRect.x - this.startX - this.borderInfo[1];
            dragY = y - parentDomRect.y - this.startY - this.borderInfo[2];
        }
        this.left = this.setBetween(dragX, 0, this.maxX - this.borderInfo[1] - this.borderInfo[3]);
        this.top = this.setBetween(dragY, 0, this.maxY - this.borderInfo[2] - this.borderInfo[0]);
        this.el.style.left = this.left + 'px';
        this.el.style.top = this.top + 'px';
        this.emitEvent('todragmove');
    }
    endEvent() {
        this.isDrag = false;
        document.removeEventListener('mousemove', this.moveEvent);
        document.removeEventListener('mouseup', this.endEvent);
        document.removeEventListener('touchmove', this.moveEvent);
        document.removeEventListener('touchend', this.endEvent);
        document.body.style.userSelect = 'auto';
        if (this.options.forbidBodyScroll) {
            document.body.style.overflow = 'visible';
        }
        this.handleAdsorb();
        this.handlePositionMode();
        this.emitEvent('todragend');
    }
    handleAdsorb() {
        if (this.options.isAbsolute)
            return;
        const endPoint = [this.left + this.width / 2, this.top + this.height / 2];
        const maxPoint = [window.innerWidth, window.innerHeight];
        this.el.style.transition = `left ${this.options.transitionDuration}s ${this.options.transitionTimingFunction}, 
                                top ${this.options.transitionDuration}s ${this.options.transitionTimingFunction}`;
        if (this.options.adsorb === 1) {
            // 左右吸附
            if (endPoint[0] <= window.innerWidth / 2) {
                // left
                this.left = this.options.adsorbOffset;
            }
            else {
                // right
                this.left = this.maxX - this.options.adsorbOffset;
            }
        }
        else if (this.options.adsorb === 2) {
            // 四方向吸附
            const k1 = maxPoint[1] / maxPoint[0];
            const k2 = maxPoint[1] / -maxPoint[0];
            const k3 = endPoint[1] / endPoint[0];
            const k4 = endPoint[1] / (endPoint[0] - maxPoint[0]);
            if (k1 >= k3 && k2 < k4) {
                // top
                this.top = this.options.adsorbOffset;
            }
            else if (k1 >= k3 && k2 >= k4) {
                // right
                this.left = this.maxX - this.options.adsorbOffset;
            }
            else if (k1 < k3 && k2 >= k4) {
                // bottom
                this.top = this.maxY - this.options.adsorbOffset;
            }
            else {
                // left
                this.left = this.options.adsorbOffset;
            }
            if (this.options.adsorbOffset) {
                if (this.top === 0) {
                    this.top = this.options.adsorbOffset;
                }
                if (this.top === this.maxY) {
                    this.top = this.maxY - this.options.adsorbOffset;
                }
                if (this.left === 0) {
                    this.left = this.options.adsorbOffset;
                }
                if (this.left === this.maxX) {
                    this.left = this.maxX - this.options.adsorbOffset;
                }
            }
        }
        this.el.style.left = this.left + 'px';
        this.el.style.top = this.top + 'px';
    }
    handlePositionMode() {
        if (this.options.adsorb)
            return;
        const left = this.options.isAbsolute ? this.el.offsetLeft : this.left;
        const top = this.options.isAbsolute ? this.el.offsetTop : this.top;
        this.right = this.maxX - left - this.borderInfo[1] - this.borderInfo[3];
        this.bottom = this.maxY - top - this.borderInfo[2] - this.borderInfo[0];
        if (this.options.positionMode === 2) {
            this.el.style.left = 'auto';
            this.el.style.right = this.right + 'px';
        }
        else if (this.options.positionMode === 3) {
            this.el.style.top = 'auto';
            this.el.style.bottom = this.bottom + 'px';
        }
        else if (this.options.positionMode === 4) {
            this.el.style.left = 'auto';
            this.el.style.top = 'auto';
            this.el.style.right = this.right + 'px';
            this.el.style.bottom = this.bottom + 'px';
        }
    }
    emitEvent(type) {
        const event = document.createEvent('HTMLEvents');
        event.initEvent(type, false, false);
        const { left, top, right, bottom, width, height, maxX, maxY } = this;
        event['left'] = left;
        event['top'] = top;
        event['width'] = width;
        event['height'] = height;
        event['maxX'] = maxX;
        event['maxY'] = maxY;
        event['right'] = right;
        event['bottom'] = bottom;
        this.el.dispatchEvent(event);
    }
    destroy() {
        if (this.isTouch) {
            this.el.removeEventListener('touchstart', this.handleTouchStart);
        }
        else {
            this.el.removeEventListener('mousedown', this.handleMousedown);
        }
    }
    setLimit() {
        if (!this.options.isAbsolute || !this.options.needComputeBorder)
            return;
        const position = ['top', 'right', 'bottom', 'left'];
        const borderInfo = getComputedStyleList(this.parent, [
            ...position.map(p => `border-${p}-width`),
        ], true);
        this.borderInfo = position.map(p => borderInfo[`border-${p}-width`]);
    }
}
const mounted = (el, binding, userOptions) => {
    const { value } = binding;
    const customGlobalOptions = userOptions || {};
    const options = Object.assign(Object.assign({}, customGlobalOptions), value);
    el.$toDarg = new ToDrag({
        el,
        options
    });
};
const unmounted = (el) => {
    el.$toDarg && el.$toDarg.destroy();
};
export const ToDragDirective = {
    mounted: (el, binding) => mounted(el, binding),
    unmounted,
    // @ts-ignore
    inserted: (el, binding) => mounted(el, binding),
    unbind: unmounted,
    install: (Vue, userOptions) => {
        Vue.directive('to-drag', {
            mounted: ((el, binding) => mounted(el, binding, userOptions)),
            unmounted,
            // @ts-ignore
            inserted: ((el, binding) => mounted(el, binding, userOptions)),
            unbind: unmounted
        });
    }
};

export default ToDrag
