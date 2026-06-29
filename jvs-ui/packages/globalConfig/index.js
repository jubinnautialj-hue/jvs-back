import globalConfig from './src'
export default Vue=>{
    Vue.component(globalConfig.name,globalConfig);
}