import Vue from 'vue'
import loginForm from '@/components/basic-container/login/loginForm.vue'
const loginFormBox = Vue.extend(loginForm)
loginForm.install = function (data, callback) {
  let instance = new loginFormBox({data})
  instance.$store = this.$root.$store
  instance.$mount()

  document.body.appendChild(instance.$el)

  Vue.nextTick(() => {
    instance.init()
  })
  callback(instance)
}

export default loginForm