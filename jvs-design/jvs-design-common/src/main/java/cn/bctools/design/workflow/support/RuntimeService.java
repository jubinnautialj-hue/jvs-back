package cn.bctools.design.workflow.support;

/**
 * @author zhuxiaokang
 * 流程任务运行时服务
 */
public interface RuntimeService {

     /**
      * 启动流程
      *
      * @param startTask 启动流程入参
      * @return 运行时数据
      */
     RuntimeData start(StartTask startTask);

     /**
      * 执行流程
      *
      * @param executeTask 执行流程入参
      * @return 运行时数据
      */
     RuntimeData execute(ExecuteTask executeTask);
}
