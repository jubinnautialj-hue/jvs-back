import Vue from 'vue'
import videoPreview from '@/components/basic-container/video/videoPreview.vue'
const videoPreviewBox = Vue.extend(videoPreview)
videoPreview.install = function (data) {
  let instance = new videoPreviewBox({data})
  instance.$store = this.$root.$store
  instance.$mount()

  document.body.appendChild(instance.$el)

  Vue.nextTick(() => {
    instance.init()
  })
}

export default videoPreview
