import { connect } from "echarts"
import { ie, ios } from "./browser.js"

export function classTest(cls) { return new RegExp("(^|\\s)" + cls + "(?:$|\\s)\\s*") }

export let rmClass = function(node, cls) {
  let current = node.className
  let match = classTest(cls).exec(current)
  if (match) {
    let after = current.slice(match.index + match[0].length)
    node.className = current.slice(0, match.index) + (after ? match[1] + after : "")
  }
}

export function removeChildren(e) {
  for (let count = e.childNodes.length; count > 0; --count)
    e.removeChild(e.firstChild)
  return e
}

export function removeChildrenAndAdd(parent, e) {
  return removeChildren(parent).appendChild(e)
}

export function elt(tag, content, className, style, nodeData) {
  let e = document.createElement(tag)
  if (className) e.className = className
  if (style) e.style.cssText = style
  
  if (typeof content == "string"){
    if(content) {
      if(nodeData) {
        let tel = document.createElement('span')
        tel.innerHTML = ( '&ZeroWidthSpace;' + content + '&ZeroWidthSpace;')
        tel.setAttribute("class", `cm-field ${(nodeData && nodeData.param) ? ('cm-field-param') : 'cm-field-function'}`)
        tel.setAttribute("title", `${(nodeData && nodeData.id) ? nodeData.id : ''}`)
        e.appendChild(tel)
        if(!nodeData.param){
          let bracket = document.createElement('span')
          bracket.innerHTML = '()'
          bracket.setAttribute("class", "cm-bracket")
          e.appendChild(bracket)
        }
      }else{
        if(!/[\u0000-\u001f\u007f-\u009f\u00ad\u061c\u200b\u200e\u200f\u2022\u2028\u2029\ufeff\ufff9-\ufffc]/g.test(content)) {
          e.appendChild(document.createTextNode(content))
        }
      }
    }
  } else if (content) {
    for (let i = 0; i < content.length; ++i) {
      e.appendChild(content[i])
    }
  }
  return e
}
// wrapper for elt, which removes the elt from the accessibility tree
export function eltP(tag, content, className, style, nodeData, alreadyDom) {
  let e = elt(tag, content, className, style, nodeData, alreadyDom)
  e.setAttribute("role", "presentation")
  return e
}

export let range
if (document.createRange) range = function(node, start, end, endNode) {
  let r = document.createRange()
  r.setEnd(endNode || node, end)
  r.setStart(node, start)
  return r
}
else range = function(node, start, end) {
  let r = document.body.createTextRange()
  try { r.moveToElementText(node.parentNode) }
  catch(e) { return r }
  r.collapse(true)
  r.moveEnd("character", end)
  r.moveStart("character", start)
  return r
}

export function contains(parent, child) {
  if (child.nodeType == 3) // Android browser always returns false when child is a textnode
    child = child.parentNode
  if (parent.contains)
    return parent.contains(child)
  do {
    if (child.nodeType == 11) child = child.host
    if (child == parent) return true
  } while (child = child.parentNode)
}

export function activeElt() {
  // IE and Edge may throw an "Unspecified Error" when accessing document.activeElement.
  // IE < 10 will throw when accessed while the page is loading or in an iframe.
  // IE > 9 and Edge will throw when accessed in an iframe if document.body is unavailable.
  let activeElement
  try {
    activeElement = document.activeElement
  } catch(e) {
    activeElement = document.body || null
  }
  while (activeElement && activeElement.shadowRoot && activeElement.shadowRoot.activeElement)
    activeElement = activeElement.shadowRoot.activeElement
  return activeElement
}

export function addClass(node, cls) {
  let current = node.className
  if (!classTest(cls).test(current)) node.className += (current ? " " : "") + cls
}
export function joinClasses(a, b) {
  let as = a.split(" ")
  for (let i = 0; i < as.length; i++)
    if (as[i] && !classTest(as[i]).test(b)) b += " " + as[i]
  return b
}

export let selectInput = function(node) { node.select() }
if (ios) // Mobile Safari apparently has a bug where select() is broken.
  selectInput = function(node) { node.selectionStart = 0; node.selectionEnd = node.value.length }
else if (ie) // Suppress mysterious IE10 errors
  selectInput = function(node) { try { node.select() } catch(_e) {} }
