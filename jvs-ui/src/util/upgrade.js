import Vue from 'vue'
import upgradeDialog from '@/components/basic-container/upgrade/upgradeDialog.vue'
const upgradeDialogBox = Vue.extend(upgradeDialog)
upgradeDialog.install = function (data) {
  console.log(data)
  let instance = new upgradeDialogBox({data})
  instance.$mount()

  document.body.appendChild(instance.$el)

  Vue.nextTick(() => {
    // debugger
    instance.init()
  })
}

export default upgradeDialog
