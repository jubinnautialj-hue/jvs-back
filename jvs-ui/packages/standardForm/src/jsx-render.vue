<script>
export default {
    name: 'JsxRender',
    props: {
      scope: Object,
      render: Function
    },
    components:{
      PositionSelector: () => import('../../positionSelector/src/index.vue'),
      StandardColorPicker: () => import('../../standardColorPicker/src/index.vue'),
      FontSelector: () => import('../../fontSelector/src'),
      BackgroundFilterSelector:() => import('../../backgroundFilterSelector/src'),
      uploadFile:() => import('../../uploadFile/src'),
      customInputNumber:() => import('./customInputNumber.vue'),
      customSelectBox:async () => import('./customSelectBox.vue'),
    },
  data() {
    return {    

    };
  },
  created() {

  },
  mounted() {
  },
  methods: {
    changeVal(val){
      this.$emit('changeVal',val)
    }
  },
  render(h){
    return this.render(h,this.scope)
  }
};
</script>
