// const ToDrag = require('./toDrag.js');
import ToDrag from './toDrag'

class ToControl extends ToDrag {
    constructor({ el, options }) {
        super({
            el,
            options: Object.assign({ adsorb: 0, adsorbOffset: 0, isAbsolute: false, positionMode: 1 }, options)
        });
        this.arrowCtx = null;
        this.arrowStartX = 0;
        this.arrowStartY = 0;
        this.elWidth = 0;
        this.elHeight = 0;
        this.resizeFlag = false;
        this.arrowMouseDownEvent = (e) => {
            if (typeof this.controlOptions.disabled === 'function' && this.controlOptions.disabled()) {
                return;
            }
            const x = this.isTouch ? e.changedTouches[0].clientX : e.x;
            const y = this.isTouch ? e.changedTouches[0].clientY : e.y;
            const { width, height, left, top } = this.el.getBoundingClientRect();
            const { width: parentWidth, height: parentHeight } = this.parent.getBoundingClientRect();
            // const parent = document.getElementById(this.controlOptions.ids)
            // const { width: parentWidth, height: parentHeight } = parent.getBoundingClientRect();
            this.arrowStartX = x;
            this.arrowStartY = y;
            this.elWidth = width;
            this.elHeight = height;
            this.resizeFlag = true;
            let maxWidth, maxHeight;
            if (this.controlOptions.isAbsolute) {
                maxWidth = parentWidth - this.el.offsetLeft - this.borderInfo[1] - this.borderInfo[3];
                maxHeight = parentHeight - this.el.offsetTop - this.borderInfo[0] - this.borderInfo[2];
            }
            else {
                maxWidth = window.innerWidth - left;
                maxHeight = window.innerHeight - top;
            }
            setTimeout(() => {
                this.isDrag = false;
            });
            this.el.style.left = `${this.controlOptions.isAbsolute ? this.el.offsetLeft : left}px`;
            this.el.style.top = `${this.controlOptions.isAbsolute ? this.el.offsetTop : top}px`;
            this.el.style.right = 'auto';
            this.el.style.bottom = 'auto';
            this.emitControlEvent('tocontrolstart');
            if (this.isTouch) {
                // 移动端
                document.ontouchmove = (e) => {
                    if (!this.resizeFlag || !e.changedTouches)
                        return;
                    const { clientX: x, clientY: y } = e.changedTouches[0];
                    this.el.style.width = `${Math.min(this.elWidth + x - this.arrowStartX, maxWidth)}px`;
                    this.el.style.height = `${Math.min(this.elHeight + y - this.arrowStartY, maxHeight)}px`;
                    this.emitControlEvent('tocontrolmove');
                };
                document.ontouchend = () => {
                    this.setPosition();
                    this.resizeFlag = false;
                    document.ontouchmove = null;
                    document.ontouchend = null;
                    this.emitControlEvent('tocontrolend');
                };
            }
            else {
                // PC端
                document.onmousemove = (e) => {
                    if (!this.resizeFlag)
                        return;
                    const { x, y } = e;
                    this.el.style.width = `${Math.min(this.elWidth + x - this.arrowStartX, maxWidth)}px`;
                    this.el.style.height = `${Math.min(this.elHeight + y - this.arrowStartY, maxHeight)}px`;
                    this.emitControlEvent('tocontrolmove');
                };
                document.onmouseup = () => {
                    this.setPosition();
                    this.resizeFlag = false;
                    document.onmousemove = null;
                    document.onmouseup = null;
                    this.emitControlEvent('tocontrolend');
                };
            }
        };
        this.controlOptions = options || {};
        this.initControl();
    }
    initControl() {
        var _a;
        this.arrowCtx = this.createResizeArrow((_a = this.controlOptions) === null || _a === void 0 ? void 0 : _a.arrowOptions);
        this.el.appendChild(this.arrowCtx);
    }
    updateArrow() {
        var _a;
        if (this.arrowCtx && this.el.contains(this.arrowCtx)) {
            this.el.removeChild(this.arrowCtx);
        }
        this.arrowCtx = this.createResizeArrow((_a = this.controlOptions) === null || _a === void 0 ? void 0 : _a.arrowOptions);
        this.el.appendChild(this.arrowCtx);
    }
    createResizeArrow(arrowOptions) {
        const isDisabled = typeof this.controlOptions.disabled === 'function' && this.controlOptions.disabled();
        const options = Object.assign({ size: 8, lineWidth: 2, lineColor: '#9a9a9a', padding: 2 }, arrowOptions);
        const arrow = document.createElement('div');
        arrow.style.cssText = `
      position: absolute;
      right: 0; 
      bottom: 0;
      padding: ${options.padding}px;
      cursor: se-resize;
      background: ${options.background || 'none'};
      display: ${isDisabled ? 'none' : 'block'}
    `;
        arrow.className = 'to-control-arrow';
        const arrowInner = document.createElement('div');
        arrowInner.style.cssText = `
      width: ${options.size}px;
      height: ${options.size}px;
      border-bottom: ${options.lineWidth}px solid ${options.lineColor};
      border-right: ${options.lineWidth}px solid ${options.lineColor};
    `;
        arrowInner.className = 'to-control-arrow-inner';
        arrow.appendChild(arrowInner);
        if (this.isTouch) {
            arrow.addEventListener('touchstart', this.arrowMouseDownEvent);
        }
        else {
            arrow.addEventListener('mousedown', this.arrowMouseDownEvent);
        }
        return arrow;
    }
    destroyControl() {
        var _a, _b;
        this.destroy();
        if (this.isTouch) {
            (_a = this.arrowCtx) === null || _a === void 0 ? void 0 : _a.removeEventListener('touchstart', this.arrowMouseDownEvent);
        }
        else {
            (_b = this.arrowCtx) === null || _b === void 0 ? void 0 : _b.removeEventListener('mousedown', this.arrowMouseDownEvent);
        }
    }
    emitControlEvent(type) {
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
}
const mounted = (el, binding, userOptions) => {
    const { value } = binding;
    const customGlobalOptions = userOptions || {};
    const options = Object.assign(Object.assign({}, customGlobalOptions), value);
    el.$toControl = new ToControl({
        el,
        options
    });
};
const beforeUpdate = (el) => {
    el.$toControl && el.$toControl.updateArrow();
};
const unmounted = (el) => {
    el.$toControl && el.$toControl.destroy();
};
export const ToControlDirective = {
    mounted: (el, binding) => mounted(el, binding),
    unmounted,
    beforeUpdate,
    // @ts-ignore
    inserted: (el, binding) => mounted(el, binding),
    unbind: unmounted,
    update: beforeUpdate,
    install: (Vue, userOptions) => {
        Vue.directive('to-control', {
            mounted: ((el, binding) => mounted(el, binding, userOptions)),
            unmounted,
            beforeUpdate,
            // @ts-ignore
            inserted: ((el, binding) => mounted(el, binding, userOptions)),
            unbind: unmounted,
            update: beforeUpdate,
        });
    }
};

// exports.ToControlDirective = ToControlDirective;
export default ToControl