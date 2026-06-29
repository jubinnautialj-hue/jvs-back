<template>
   <svg
    xmlns='http://www.w3.org/2000/svg'
    :viewBox="viewBox"
    :fill="fill"
    :height="size"
    :width="size">
    <path :d="icon" />
  </svg>
</template>

<script>
import IconMap from '@/const/icon'
export default {
    name:"Icon",
    props:{
        name: {
            type: String,
            default: 'unknown'
        },
        viewBox: {
            type: String,
            default: '0 0 24 24',
        },
        size: {
            type: [String, Number],
            default: 24,
        },
        fill: {
            type: String,
            default: 'currentColor',
        }
    },
  data() {
    return {

    };
  },
  created() {

  },
  computed:{
    icon(){
        return IconMap[this.name]
    }
  },
  mounted() {

  },
  methods: {

  }
};
</script>

<style scoped lang="scss">

</style>
