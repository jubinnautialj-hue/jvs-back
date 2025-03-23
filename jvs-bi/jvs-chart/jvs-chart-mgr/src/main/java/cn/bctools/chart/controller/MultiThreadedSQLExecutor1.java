package cn.bctools.chart.controller;

import cn.hutool.core.util.NumberUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultiThreadedSQLExecutor1 {

    // MySQL数据库的连接信息
    private static final String JDBC_URL = "jdbc:mysql://10.0.0.201:3306/flink-test";
    private static final String JDBC_USER = "root";
    private static final String JDBC_PASSWORD = "root";

    // SQL语句
    private static final String SQL_INSERT = "INSERT INTO sys_log_100000000 (\n" +
            "\t`handle_user`,\n" +
            "\t`user_id`,\n" +
            "\t`client_name`,\n" +
            "\t`client_id`,\n" +
            "\t`operation_type`,\n" +
            "\t`create_date`,\n" +
            "\t`tenant_id`,\n" +
            "\t`user_name`,\n" +
            "\t`type`,\n" +
            "\t`api`,\n" +
            "\t`env`,\n" +
            "\t`exception_message`,\n" +
            "\t`elements`,\n" +
            "\t`ip`,\n" +
            "\t`status`,\n" +
            "\t`return_obj`,\n" +
            "\t`consuming_time`,\n" +
            "\t`version`,\n" +
            "\t`tid`,\n" +
            "\t`class_name`,\n" +
            "\t`method_name`,\n" +
            "\t`end_time`,\n" +
            "\t`start_time`,\n" +
            "\t`function_name` \n" +
            ")\n" +
            "VALUES\n" +
            "\t(\n" +
            "\t\t'F2UQvG3CSy',\n" +
            "\t\t'xiuyingyan',\n" +
            "\t\t'Yan Xiuying',\n" +
            "\t\t'rQU2EYRfzG',\n" +
            "\t\t'Fw9LbJmR7I',\n" +
            "\t\t'2001-11-01 21:30:45',\n" +
            "\t\t'ie8qzSu5Vu',\n" +
            "\t\t'Yan Xiuying',\n" +
            "\t\t1,\n" +
            "\t\t'With its well-designed Graphical User Interface(GUI), Navicat lets you quickly and easily create,',\n" +
            "\t'WgLrU0gei4',\n" +
            "\t'Navicat Monitor',\n" +
            "\t'In the Objects tab',\n" +
            "\t'251.100.1.219',\n" +
            "\t0,\n" +
            "\t'Monitored',\n" +
            "\t767,\n" +
            "\t'6PCtH6lBZS',\n" +
            "\t'UHXUAkOh2t',\n" +
            "\t'Yan Xiuying',\n" +
            "\t'Yan Xiuying',\n" +
            "\t'2021-03-19 04:03:12',\n" +
            "\t'2018-03-02 01:13:38',\n" +
            "\t'Yan Xiuying' \n" +
            ")," +
            "\t(\n" +
            "\t\t'F2UQvG3CSy',\n" +
            "\t\t'xiuyingyan',\n" +
            "\t\t'Yan Xiuying',\n" +
            "\t\t'rQU2EYRfzG',\n" +
            "\t\t'Fw9LbJmR7I',\n" +
            "\t\t'2001-11-01 21:30:45',\n" +
            "\t\t'ie8qzSu5Vu',\n" +
            "\t\t'Yan Xiuying',\n" +
            "\t\t1,\n" +
            "\t\t'With its well-designed Graphical User Interface(GUI), Navicat lets you quickly and easily create,',\n" +
            "\t'WgLrU0gei4',\n" +
            "\t'Navicat Monitor',\n" +
            "\t'In the Objects tab',\n" +
            "\t'251.100.1.219',\n" +
            "\t0,\n" +
            "\t'Monitored',\n" +
            "\t767,\n" +
            "\t'6PCtH6lBZS',\n" +
            "\t'UHXUAkOh2t',\n" +
            "\t'Yan Xiuying',\n" +
            "\t'Yan Xiuying',\n" +
            "\t'2021-03-19 04:03:12',\n" +
            "\t'2018-03-02 01:13:38',\n" +
            "\t'Yan Xiuying' \n" +
            "),"+"\t(\n" +
            "\t\t'F2UQvG3CSy',\n" +
            "\t\t'xiuyingyan',\n" +
            "\t\t'Yan Xiuying',\n" +
            "\t\t'rQU2EYRfzG',\n" +
            "\t\t'Fw9LbJmR7I',\n" +
            "\t\t'2001-11-01 21:30:45',\n" +
            "\t\t'ie8qzSu5Vu',\n" +
            "\t\t'Yan Xiuying',\n" +
            "\t\t1,\n" +
            "\t\t'With its well-designed Graphical User Interface(GUI), Navicat lets you quickly and easily create,',\n" +
            "\t'WgLrU0gei4',\n" +
            "\t'Navicat Monitor',\n" +
            "\t'In the Objects tab',\n" +
            "\t'251.100.1.219',\n" +
            "\t0,\n" +
            "\t'Monitored',\n" +
            "\t767,\n" +
            "\t'6PCtH6lBZS',\n" +
            "\t'UHXUAkOh2t',\n" +
            "\t'Yan Xiuying',\n" +
            "\t'Yan Xiuying',\n" +
            "\t'2021-03-19 04:03:12',\n" +
            "\t'2018-03-02 01:13:38',\n" +
            "\t'Yan Xiuying' \n" +
            "),"
            +"\t(\n" +
            "\t\t'F2UQvG3CSy',\n" +
            "\t\t'xiuyingyan',\n" +
            "\t\t'Yan Xiuying',\n" +
            "\t\t'rQU2EYRfzG',\n" +
            "\t\t'Fw9LbJmR7I',\n" +
            "\t\t'2001-11-01 21:30:45',\n" +
            "\t\t'ie8qzSu5Vu',\n" +
            "\t\t'Yan Xiuying',\n" +
            "\t\t1,\n" +
            "\t\t'With its well-designed Graphical User Interface(GUI), Navicat lets you quickly and easily create,',\n" +
            "\t'WgLrU0gei4',\n" +
            "\t'Navicat Monitor',\n" +
            "\t'In the Objects tab',\n" +
            "\t'251.100.1.219',\n" +
            "\t0,\n" +
            "\t'Monitored',\n" +
            "\t767,\n" +
            "\t'6PCtH6lBZS',\n" +
            "\t'UHXUAkOh2t',\n" +
            "\t'Yan Xiuying',\n" +
            "\t'Yan Xiuying',\n" +
            "\t'2021-03-19 04:03:12',\n" +
            "\t'2018-03-02 01:13:38',\n" +
            "\t'Yan Xiuying' \n" +
            "),"+"\t(\n" +
            "\t\t'F2UQvG3CSy',\n" +
            "\t\t'xiuyingyan',\n" +
            "\t\t'Yan Xiuying',\n" +
            "\t\t'rQU2EYRfzG',\n" +
            "\t\t'Fw9LbJmR7I',\n" +
            "\t\t'2001-11-01 21:30:45',\n" +
            "\t\t'ie8qzSu5Vu',\n" +
            "\t\t'Yan Xiuying',\n" +
            "\t\t1,\n" +
            "\t\t'With its well-designed Graphical User Interface(GUI), Navicat lets you quickly and easily create,',\n" +
            "\t'WgLrU0gei4',\n" +
            "\t'Navicat Monitor',\n" +
            "\t'In the Objects tab',\n" +
            "\t'251.100.1.219',\n" +
            "\t0,\n" +
            "\t'Monitored',\n" +
            "\t767,\n" +
            "\t'6PCtH6lBZS',\n" +
            "\t'UHXUAkOh2t',\n" +
            "\t'Yan Xiuying',\n" +
            "\t'Yan Xiuying',\n" +
            "\t'2021-03-19 04:03:12',\n" +
            "\t'2018-03-02 01:13:38',\n" +
            "\t'Yan Xiuying' \n" +
            "),"+"\t(\n" +
            "\t\t'F2UQvG3CSy',\n" +
            "\t\t'xiuyingyan',\n" +
            "\t\t'Yan Xiuying',\n" +
            "\t\t'rQU2EYRfzG',\n" +
            "\t\t'Fw9LbJmR7I',\n" +
            "\t\t'2001-11-01 21:30:45',\n" +
            "\t\t'ie8qzSu5Vu',\n" +
            "\t\t'Yan Xiuying',\n" +
            "\t\t1,\n" +
            "\t\t'With its well-designed Graphical User Interface(GUI), Navicat lets you quickly and easily create,',\n" +
            "\t'WgLrU0gei4',\n" +
            "\t'Navicat Monitor',\n" +
            "\t'In the Objects tab',\n" +
            "\t'251.100.1.219',\n" +
            "\t0,\n" +
            "\t'Monitored',\n" +
            "\t767,\n" +
            "\t'6PCtH6lBZS',\n" +
            "\t'UHXUAkOh2t',\n" +
            "\t'Yan Xiuying',\n" +
            "\t'Yan Xiuying',\n" +
            "\t'2021-03-19 04:03:12',\n" +
            "\t'2018-03-02 01:13:38',\n" +
            "\t'Yan Xiuying' \n" +
            ")," +
            "\t(\n" +
            "\t\t'F2UQvG3CSy',\n" +
            "\t\t'xiuyingyan',\n" +
            "\t\t'Yan Xiuying',\n" +
            "\t\t'rQU2EYRfzG',\n" +
            "\t\t'Fw9LbJmR7I',\n" +
            "\t\t'2001-11-01 21:30:45',\n" +
            "\t\t'ie8qzSu5Vu',\n" +
            "\t\t'Yan Xiuying',\n" +
            "\t\t1,\n" +
            "\t\t'With its well-designed Graphical User Interface(GUI), Navicat lets you quickly and easily create,',\n" +
            "\t'WgLrU0gei4',\n" +
            "\t'Navicat Monitor',\n" +
            "\t'In the Objects tab',\n" +
            "\t'251.100.1.219',\n" +
            "\t0,\n" +
            "\t'Monitored',\n" +
            "\t767,\n" +
            "\t'6PCtH6lBZS',\n" +
            "\t'UHXUAkOh2t',\n" +
            "\t'Yan Xiuying',\n" +
            "\t'Yan Xiuying',\n" +
            "\t'2021-03-19 04:03:12',\n" +
            "\t'2018-03-02 01:13:38',\n" +
            "\t'Yan Xiuying' \n" +
            "),"+"\t(\n" +
            "\t\t'F2UQvG3CSy',\n" +
            "\t\t'xiuyingyan',\n" +
            "\t\t'Yan Xiuying',\n" +
            "\t\t'rQU2EYRfzG',\n" +
            "\t\t'Fw9LbJmR7I',\n" +
            "\t\t'2001-11-01 21:30:45',\n" +
            "\t\t'ie8qzSu5Vu',\n" +
            "\t\t'Yan Xiuying',\n" +
            "\t\t1,\n" +
            "\t\t'With its well-designed Graphical User Interface(GUI), Navicat lets you quickly and easily create,',\n" +
            "\t'WgLrU0gei4',\n" +
            "\t'Navicat Monitor',\n" +
            "\t'In the Objects tab',\n" +
            "\t'251.100.1.219',\n" +
            "\t0,\n" +
            "\t'Monitored',\n" +
            "\t767,\n" +
            "\t'6PCtH6lBZS',\n" +
            "\t'UHXUAkOh2t',\n" +
            "\t'Yan Xiuying',\n" +
            "\t'Yan Xiuying',\n" +
            "\t'2021-03-19 04:03:12',\n" +
            "\t'2018-03-02 01:13:38',\n" +
            "\t'Yan Xiuying' \n" +
            "),"
            +"\t(\n" +
            "\t\t'F2UQvG3CSy',\n" +
            "\t\t'xiuyingyan',\n" +
            "\t\t'Yan Xiuying',\n" +
            "\t\t'rQU2EYRfzG',\n" +
            "\t\t'Fw9LbJmR7I',\n" +
            "\t\t'2001-11-01 21:30:45',\n" +
            "\t\t'ie8qzSu5Vu',\n" +
            "\t\t'Yan Xiuying',\n" +
            "\t\t1,\n" +
            "\t\t'With its well-designed Graphical User Interface(GUI), Navicat lets you quickly and easily create,',\n" +
            "\t'WgLrU0gei4',\n" +
            "\t'Navicat Monitor',\n" +
            "\t'In the Objects tab',\n" +
            "\t'251.100.1.219',\n" +
            "\t0,\n" +
            "\t'Monitored',\n" +
            "\t767,\n" +
            "\t'6PCtH6lBZS',\n" +
            "\t'UHXUAkOh2t',\n" +
            "\t'Yan Xiuying',\n" +
            "\t'Yan Xiuying',\n" +
            "\t'2021-03-19 04:03:12',\n" +
            "\t'2018-03-02 01:13:38',\n" +
            "\t'Yan Xiuying' \n" +
            "),"+"\t(\n" +
            "\t\t'F2UQvG3CSy',\n" +
            "\t\t'xiuyingyan',\n" +
            "\t\t'Yan Xiuying',\n" +
            "\t\t'rQU2EYRfzG',\n" +
            "\t\t'Fw9LbJmR7I',\n" +
            "\t\t'2001-11-01 21:30:45',\n" +
            "\t\t'ie8qzSu5Vu',\n" +
            "\t\t'Yan Xiuying',\n" +
            "\t\t1,\n" +
            "\t\t'With its well-designed Graphical User Interface(GUI), Navicat lets you quickly and easily create,',\n" +
            "\t'WgLrU0gei4',\n" +
            "\t'Navicat Monitor',\n" +
            "\t'In the Objects tab',\n" +
            "\t'251.100.1.219',\n" +
            "\t0,\n" +
            "\t'Monitored',\n" +
            "\t767,\n" +
            "\t'6PCtH6lBZS',\n" +
            "\t'UHXUAkOh2t',\n" +
            "\t'Yan Xiuying',\n" +
            "\t'Yan Xiuying',\n" +
            "\t'2021-03-19 04:03:12',\n" +
            "\t'2018-03-02 01:13:38',\n" +
            "\t'Yan Xiuying' \n" +
            ")"; // 请将...替换为实际的列名和值

    public static void main(String[] args) {
        // 创建一个具有10个线程的线程池
        ExecutorService executorService = Executors.newFixedThreadPool(80);

        // 提交10个任务，每个任务执行1万次SQL插入
        for (int i = 0; i < 80; i++) {
            executorService.submit(new SQLTask());
        }

        // 关闭线程池（这将在所有任务完成后发生）
        executorService.shutdown();
    }

    // 定义一个实现Runnable接口的任务类
    static class SQLTask implements Runnable {
        @Override
        public void run() {
            try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {
                // 关闭自动提交，以提高批量插入的性能
//                connection.setAutoCommit(false);

                // 预处理SQL语句
                PreparedStatement preparedStatement = connection.prepareStatement(SQL_INSERT);

                // 每个线程执行1万次插入
                for (int i = 0; i < 10000; i++) {
                    preparedStatement.executeUpdate();
                    System.out.println("写入成功,当前线程id为:"+Thread.currentThread().getId()+"。当前进度为:"+ NumberUtil.div(i+1,100.00,2) +"%");
                }

            } catch (SQLException e) {
                e.printStackTrace();
                // 根据需要处理异常，例如记录日志或重试
            }
        }
    }
}