package com.huyi.web.controller;

import com.huyi.common.aop.define.MethodLogLevel;
import com.huyi.common.aop.define.anno.MethodLog;
import com.huyi.common.dto.HYResult;
import com.huyi.common.utils.JsonUtils;
import com.huyi.web.config.RabbitMqConfig;
import com.huyi.web.entity.PlanEntity;
import com.huyi.web.entity.TaskEntity;
import com.huyi.web.enums.PlanType;
import com.huyi.web.enums.TaskType;
import com.huyi.web.handle.ReportHandle;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.concurrent.ThreadLocalRandom.current;

/** @Author huyi @Date 2020/8/25 13:41 @Description: 测试 controller */
@Api(value = "测试接口", tags = "测试")
@RestController
@CrossOrigin
public class TestController {
  @Autowired private AmqpTemplate rabbitTemplate;
  @Autowired private ReportHandle reportHandle;

  private static AtomicInteger taskId = new AtomicInteger(0);
  private static AtomicInteger planId = new AtomicInteger(0);
  private static List<String> params =
      Stream.of(
              "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q",
              "r", "s", "t", "u", "v", "w", "x", "y", "z")
          .collect(Collectors.toList());

  @ApiOperation(value = "暂停", httpMethod = "GET")
  @ApiResponse(code = 200, message = "success", response = ResponseEntity.class)
  @ApiImplicitParams({})
  @RequestMapping(
      value = "/stop",
      method = RequestMethod.GET,
      produces = "application/json;charset=UTF-8")
  @ResponseBody
  @MethodLog(methodLogLevel = MethodLogLevel.INFO)
  public ResponseEntity stop(@RequestParam Integer planId, @RequestParam Integer userId) {
    rabbitTemplate.convertAndSend(
        RabbitMqConfig.PLAN_QUEUE,
        JsonUtils.bean2Json(
            PlanEntity.builder()
                .planId(planId)
                .status(PlanType.STOP.getCode())
                .userId(userId)
                .build()));
    return new ResponseEntity<>(new HYResult("OK").success(), HttpStatus.OK);
  }

  @ApiOperation(value = "恢复", httpMethod = "GET")
  @ApiResponse(code = 200, message = "success", response = ResponseEntity.class)
  @ApiImplicitParams({})
  @RequestMapping(
      value = "/reRun",
      method = RequestMethod.GET,
      produces = "application/json;charset=UTF-8")
  @ResponseBody
  @MethodLog(methodLogLevel = MethodLogLevel.INFO)
  public ResponseEntity reRun(@RequestParam Integer planId, @RequestParam Integer userId) {
    rabbitTemplate.convertAndSend(
        RabbitMqConfig.PLAN_QUEUE,
        JsonUtils.bean2Json(
            PlanEntity.builder()
                .planId(planId)
                .status(PlanType.RUNNING.getCode())
                .userId(userId)
                .build()));
    return new ResponseEntity<>(new HYResult("OK").success(), HttpStatus.OK);
  }

  @ApiOperation(value = "查询计划", httpMethod = "GET")
  @ApiResponse(code = 200, message = "success", response = ResponseEntity.class)
  @ApiImplicitParams({})
  @RequestMapping(
      value = "/queryReport",
      method = RequestMethod.GET,
      produces = "application/json;charset=UTF-8")
  @ResponseBody
  @MethodLog(methodLogLevel = MethodLogLevel.INFO)
  public ResponseEntity queryReport(@RequestParam Integer planId) {

    return new ResponseEntity<>(
        new HYResult(reportHandle.getReport(planId)).success(), HttpStatus.OK);
  }

  @ApiOperation(value = "新增随机任务", httpMethod = "GET")
  @ApiResponse(code = 200, message = "success", response = ResponseEntity.class)
  @ApiImplicitParams({})
  @RequestMapping(
      value = "/insert",
      method = RequestMethod.GET,
      produces = "application/json;charset=UTF-8")
  @ResponseBody
  @MethodLog(methodLogLevel = MethodLogLevel.INFO)
  public ResponseEntity insert(@RequestParam Integer userId) {
    int taskSize = current().nextInt(100, 300);
    PlanEntity planEntity =
        PlanEntity.builder()
            .userId(userId)
            .planId(planId.incrementAndGet())
            .planTime(System.currentTimeMillis())
            .level(0)
            .robotSize(current().nextInt(20, 30))
            .taskAmount(taskSize)
            .status(PlanType.READY.getCode())
            .msg("测试任务1")
            .build();
    rabbitTemplate.convertAndSend(RabbitMqConfig.PLAN_QUEUE, JsonUtils.bean2Json(planEntity));
    IntStream.range(0, taskSize)
        .forEach(
            (x) -> {
              TaskEntity taskEntity =
                  TaskEntity.builder()
                      .taskId(taskId.incrementAndGet())
                      .param(getParam())
                      .planId(planId.get())
                      .status(TaskType.RUNNING.getCode())
                      .amount(current().nextLong(1000L))
                      .build();
              rabbitTemplate.convertAndSend(
                  RabbitMqConfig.TASK_QUEUE, JsonUtils.bean2Json(taskEntity));
            });
    return new ResponseEntity<>(new HYResult("OK").success(), HttpStatus.OK);
  }

  private String getParam() {
    return new StringBuffer()
        .append(params.get(current().nextInt(params.size())))
        .append(params.get(current().nextInt(params.size())))
        .append(params.get(current().nextInt(params.size())))
        .toString();
  }
}
