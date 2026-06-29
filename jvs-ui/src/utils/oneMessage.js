import { Message } from 'element-ui';
let messageInstance = null;
let messageInstanceArr = []
const resetMessage = (options) => {
  let deleteMessage =  (o,type)=>{
    for(let i = 0;i<messageInstanceArr.length;i++){
      if(messageInstanceArr[i].options.message == o.message && messageInstanceArr[i].options.type == o.type){
        if(type=='delete'){
          messageInstanceArr[i].messageInstance.close()
        }
        if(type=='close'){
          messageInstanceArr.splice(i,1)
        }
        break
      }
    }
  }
  if(messageInstanceArr.length>0){
    deleteMessage(options,'delete')
  }
  options.showClose = true;
  options.onClose = function(){
    deleteMessage(options,'close')
  }
  messageInstance = Message(options);
  messageInstanceArr.push({
    options,
    messageInstance
  })

  // if (messageInstance && options.message == messageInstance.message) {
  //   messageInstance.close();
  // }
  // options.showClose = true;
  // setTimeout(()=>{
  //   messageInstance = Message(options);
  // },1);
};
['error', 'success', 'info', 'warning'].forEach(type => {
  resetMessage[type] = options => {
    if (typeof options === 'string') {
      options = {
        message: options
      };
    }
    options.type = type;
    return resetMessage(options);
  };
});
export default resetMessage;
