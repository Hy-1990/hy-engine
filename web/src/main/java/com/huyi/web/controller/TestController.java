package com.huyi.web.controller;

import com.huyi.common.aop.define.MethodLogLevel;
import com.huyi.common.aop.define.anno.MethodLog;
import com.huyi.common.dto.HYResult;
import com.huyi.common.utils.JsonUtils;
import com.huyi.web.config.RabbitMqConfig;
import com.huyi.web.entity.TaskEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** @Author huyi @Date 2020/8/25 13:41 @Description: 测试 controller */
@Api(value = "测试接口", tags = "测试")
@RestController
@CrossOrigin
public class TestController {
  @Autowired private AmqpTemplate rabbitTemplate;

  @ApiOperation(value = "测试接口1", httpMethod = "POST")
  @ApiResponse(code = 200, message = "success", response = ResponseEntity.class)
  @ApiImplicitParams({})
  @RequestMapping(
      value = "/test1",
      method = RequestMethod.POST,
      produces = "application/json;charset=UTF-8")
  @ResponseBody
  @MethodLog(methodLogLevel = MethodLogLevel.INFO)
  public ResponseEntity test1(@RequestParam String taskName, @RequestParam String msg) {
    rabbitTemplate.convertAndSend(
        RabbitMqConfig.TASK_QUEUE,
        JsonUtils.bean2Json(TaskEntity.builder().taskName(taskName).msg(msg).build()));
    return new ResponseEntity<>(new HYResult("OK").success(), HttpStatus.OK);
  }
}
