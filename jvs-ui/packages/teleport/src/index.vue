<template>
    <div>
        <slot></slot>
    </div>
</template>
<script>  
    export default {  
        name: 'teleport',  
        props: {  
            /* 移动至哪个标签内，最好使用id */  
            to: {  
                type: String,  
                required: true  
            }  
        },  
  
        mounted() {  
            document.querySelector(this.to).appendChild(this.$el)  
        },  
  
        beforeDestroy() {
            try {
                document.querySelector(this.to).removeChild(this.$el)  
            } catch (error) {
                
            }
        },  
  
        render() {  
            return <div>{this.$scopedSlots.default()}</div>  
        }  
    }  
</script> 