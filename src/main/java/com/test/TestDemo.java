package com.test;

import com.pojo.Robot;
import com.pojo.Task;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestDemo {
    public static void main(String[] args) throws Exception {
        Class.forName("com.mysql.jdbc.Driver");
        String url = "jdbc:mysql://127.0.0.1:3306/robot_schduling?useSSL=false";
        String username = "root";
        String password = "888888";
        Connection conn = DriverManager.getConnection(url, username, password);
        //第一次获取机器人信息
        String sql1 = "select * from robot;";
        //第一次获取任务信息
        String sql2 = "select * from task;";
        PreparedStatement pst1 = conn.prepareStatement(sql1);
        PreparedStatement pst2 = conn.prepareStatement(sql2);
        ResultSet rs1 = pst1.executeQuery();
        ResultSet rs2 = pst2.executeQuery();
        //根据机器人电量创建四个机器人集合对象
        List<Robot> highRobotArray = new ArrayList<>();
        List<Robot> middleRobotArray = new ArrayList<>();
        List<Robot> lowRobotArray = new ArrayList<>();
        List<Robot> surplusRobotArray = new ArrayList<>();

        //往四个机器人集合里存放数据
        Robot r = null;
        while (rs1.next()) {
            int robotID = rs1.getInt("robotID");
            double electricity = rs1.getDouble("electricity");
            int status = rs1.getInt("status");
            int electricityStatus = rs1.getInt("electricityStatus");
            r = new Robot(robotID, electricity, status, electricityStatus, null);
            //总机器人集合,低电量机器人立马充电，不加入集合
            if (electricity < 20) {
                //电量低于20，低电量机器人立马冲电，将ElectricityStatus状态置为1
                r.setElectricityStatus(1);
                lowRobotArray.add(r);
                continue;
            } else if (electricity > 50) {
                highRobotArray.add(r);
            } else {
                middleRobotArray.add(r);
            }
            surplusRobotArray.add(r);
        }
        //记录各电量机器人个数
        int highRobotCount = highRobotArray.size();
        int middleRobotCount = middleRobotArray.size();
        int lowRobotCount = lowRobotArray.size();

        //创建三个任务集合对象
        List<Task> nearTaskArray = new ArrayList<>();
        List<Task> farTaskArray = new ArrayList<>();
        List<Task> surplusTaskArray = new ArrayList<>();
        //根据近远点往三个任务集合里存放数据
        while (rs2.next()) {
            int taskID = rs2.getInt("taskID");
            double x = rs2.getDouble("x");
            double y = rs2.getDouble("y");
            String area = rs2.getString("area");
            String distance = rs2.getString("distance");
            int status = rs2.getInt("status");
            Task t = new Task(taskID, x, y, area, distance, status);
            //任务总集合
            surplusTaskArray.add(t);
            if (t.getDistance().equals("far")) {
                farTaskArray.add(t);
            } else {
                nearTaskArray.add(t);
            }
        }

        //记录近远点以及剩余任务个数
        int nearCount = nearTaskArray.size();
        int farCount = farTaskArray.size();
        int taskCount = surplusTaskArray.size();

        //记录要完成近点任务需要的机器人个数,也就是近点任务可以分成多少组（每组包含三个任务）
        int nearCountRobot;
        boolean flag;
        if (nearCount % 3 == 0) {
            nearCountRobot = nearCount / 3;
            flag = true;
        } else {
            nearCountRobot = nearCount / 3 + 1;
            flag = false;
        }

        //第一次分配任务，判断中电量机器人个数是否够完成近点任务
        System.out.println("......正在给中电量机器人安排近点任务......");
//        List<Task> threeTasks = getNearTask(nearTaskArray);

//        nearTaskArray = deleteTask(nearTaskArray);

        //如果需要的机器人个数少于中电量机器人个数的三倍（此时近点任务可以全部被分配）
        if (nearCountRobot <= middleRobotCount) {
            Task task = null;
            Robot robot = null;
            for (int i = 0; i < nearCountRobot-1 ; i++) {
                //从近点任务栏取三个任务
                List<Task> threeTaskList = getNearTask(nearTaskArray);
                //将该三个任务状态置为1
                for (Task t : threeTaskList) {
                    t.setStatus(1);
                }
                //依次给中电量机器人安排近点任务
                robot = middleRobotArray.get(i);
                //机器人接收该任务
                robot.setTask(threeTaskList);
                //机器人开始工作，状态置为1
                robot.setStatus(1);
                //从近点任务删除该三个任务
                nearTaskArray = deleteTask(nearTaskArray);
                //近点任务减3
                nearCount-=3;
            }
            //删除已有任务的机器人
            List<Robot> temRobotArray = deleteRobot(surplusRobotArray);
            surplusRobotArray = temRobotArray;
            System.out.println("近点任务安排完毕,还有剩余中电量机器人" + middleRobotCount + "台");
            //中电量机器人不够，近点任务还有剩余
        } else {
            Task task = null;
            Robot robot = null;
            for (int i = 0; i < middleRobotCount ; i++) {
                //从近点任务栏取三个任务
                List<Task> threeTaskList = getNearTask(nearTaskArray);
                //将该三个任务状态置为1
                for (Task t : threeTaskList) {
                    t.setStatus(1);
                }
                //依次给中电量机器人安排近点任务
                robot = middleRobotArray.get(i);
                //机器人接收该任务
                robot.setTask(threeTaskList);
                //机器人开始工作，状态置为1
                robot.setStatus(1);
                //从近点任务删除该三个任务
                nearTaskArray = deleteTask(nearTaskArray);
                //近点任务减3
                nearCount-=3;
            }
            //这种情况下中电量机器人为0
            middleRobotCount=0;
            //删除已有任务的机器人
            List<Robot> temRobotArray = deleteRobot(surplusRobotArray);
            surplusRobotArray = temRobotArray;
            System.out.println("中电量机器人都已被安排任务，还剩余近点任务" + nearCount + "个");
        }
        System.out.println("第一次任务分配完毕");
        System.out.println("-----------------");
        System.out.println("-----------------");
        System.out.println("正在进行第二次任务分配");
//        //记录剩余任务与剩余机器人个数
//        int surplusRobotCount = surplusRobotArray.size();
//        int surplusTaskCount = surplusTaskArray.size();

//        //若剩余机器人数量多于或等于剩余任务个数，则第二次分配即可完成任务
//        if (surplusTaskCount <= surplusRobotCount) {
//            Task task = null;
//            Robot robot = null;
//            for (int i = 0; i < surplusTaskArray.size(); i++) {
//                task = surplusTaskArray.get(i);
//                //该任务已被机器人接收，将任务状态置为1表示已完成
//                task.setStatus(1);
//                robot = surplusRobotArray.get(i);
//                //机器人接收该任务
//                robot.setTask(task);
//                //机器人开始工作，状态置为1
//                robot.setStatus(1);
//            }
//            //删除总任务中已完成的任务
//            List<Task> temTaskArray = deleteTask(surplusTaskArray);
//            surplusTaskArray = temTaskArray;
//            //删除已有任务的机器人
//            List<Robot> temRobotArray = deleteRobot(surplusRobotArray);
//            surplusRobotArray = temRobotArray;
//            System.out.println("所有任务都安排完毕");
//        } else {
//            //任务数多于机器人数，第二轮分配也不能结束
//            Task task = null;
//            Robot robot = null;
//            for (int i = 0; i < surplusRobotArray.size(); i++) {
//                task = surplusTaskArray.get(i);
//                //该任务已被机器人接收，将任务状态置为1表示已完成
//                task.setStatus(1);
//                robot = surplusRobotArray.get(i);
//                //机器人接收该任务
//                robot.setTask(task);
//                //机器人开始工作，状态置为1
//                robot.setStatus(1);
//            }
//            //删除总任务中已完成的任务
//            List<Task> temTaskArray = deleteTask(surplusTaskArray);
//            surplusTaskArray = temTaskArray;
//            //删除已有任务的机器人
//            List<Robot> temRobotArray = deleteRobot(surplusRobotArray);
//            surplusRobotArray = temRobotArray;
//            System.out.println("没有空闲的机器人了，还有任务没有完成");
//
//        }
        System.out.println("第二次任务分配完毕");
        System.out.println("--------------");
        System.out.println("--------------");
        System.out.println("--------------");
        System.out.println("高电量机器人：" + highRobotArray);
        System.out.println("中电量机器人：" + middleRobotArray);
        System.out.println("低电量机器人：" + lowRobotArray);
        System.out.println("剩余机器人集合" + surplusRobotArray);
        System.out.println("近点任务" + nearTaskArray);
        System.out.println("远点任务" + farTaskArray);
//        System.out.println("剩余任务集合" + surplusTaskArray);
        //释放资源
        rs1.close();
        rs2.close();
        pst1.close();
        conn.close();
    }

    //删除总任务中已完成的任务
    public static List<Task> deleteTask(List<Task> taskArray) {
        List<Task> temTaskArray = taskArray.stream().filter(t -> t.getStatus() == 0).collect(Collectors.toList());
        return temTaskArray;
    }

    //删除总任务中已完成的任务
    public static List<Robot> deleteRobot(List<Robot> robotArray) {
        List<Robot> temRobotArray = robotArray.stream().filter(r -> r.getStatus() == 0).collect(Collectors.toList());
        return temRobotArray;
    }

    //将近期任务取前三个任务放到一起，返回一个List集合
    public static List<Task> getNearTask(List<Task> nearTaskArray) {
        Stream<Task> stream = nearTaskArray.stream();
        List<Task> list = stream.limit(3).collect(Collectors.toList());
        return list;
    }

    //删除前三个已完成的任务
    public static List<Task> delete3Task(List<Task> nearTaskArray) {
        List<Task> temTaskArray = nearTaskArray.stream().skip(3).collect(Collectors.toList());
        return temTaskArray;
    }
}

