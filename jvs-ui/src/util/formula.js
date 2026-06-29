import Vue from 'vue'
import formula from '@/components/basic-container/formula/index.vue'
const formulaBox = Vue.extend(formula)
formula.install = function (data) {
  let instance = new formulaBox({data})
  instance.$store = this.$root.$store
  instance.$mount()

  document.body.appendChild(instance.$el)

  Vue.nextTick(() => {
    instance.init()
  })
}

export default formula