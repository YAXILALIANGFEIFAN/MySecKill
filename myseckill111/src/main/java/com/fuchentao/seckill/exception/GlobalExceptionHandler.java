package com.fuchentao.seckill.exception;

/*
定义一个全局的异常处理
登陆反馈


 */

import com.fuchentao.seckill.result.CodeMsg;
import com.fuchentao.seckill.result.Result;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/*
@ControllerAdvice 增强型控制器，对于控制器的全局配置放在同一个位置，
                  全局异常的注解，放在类上
@ControllerAdvice 默认只会处理controller层抛出的异常，如果需要处理service层的异常，
                  需要定义一个自定义的MyException来继承RuntimeException类，
                  然后@ExceptionHandler（MyException）

只有代码出错或者throw出来的异常才会被捕捉处理，如果被catch的异常，
就不会被捕捉，除非catch之后再throw异常

 */

@ControllerAdvice
@ResponseBody
public class GlobalExceptionHandler {

    //拦截所有的异常，并进行处理
    //@ExceptionHandler：指明需要处理的异常类型以及子类。注解放在方法上面
    @ExceptionHandler(value = Exception.class)
    public Result<String> exceptionHandler
        (HttpServletRequest request, Exception exception) {

        exception.printStackTrace();
        //处理来自service层的异常
        if (exception instanceof GlobalException) {
            GlobalException globalException = (GlobalException) exception;
            return Result.error(globalException.getCodeMsg());
        }
        //处理来自controller层的异常
        else if (exception instanceof BindException) {
            BindException bindException = (BindException)exception;
            List<ObjectError> errors = bindException.getAllErrors();
            ObjectError error = errors.get(0);

            String msg = error.getDefaultMessage();
            return Result.error(CodeMsg.bindError.fillArgs(msg));
        }
        else {
            return Result.error(CodeMsg.serverError);
        }
    }
}
