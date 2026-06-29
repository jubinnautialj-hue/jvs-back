import {guid} from "@/util/util";

export default class assembly {
    constructor (type,label,span){
      this.prop = type + new Date().getTime()
      this.type = type
      this.label = label
      this.span = 12
      this.id = guid()
      this.display = true
      this.status = ''
      this.tips = {
        text: "",
        position: "right"
      }
    }
  }
