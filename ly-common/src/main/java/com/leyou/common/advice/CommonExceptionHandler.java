package com.leyou.common.advice;


import com.leyou.common.enuums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.ExceptionResule;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice    //拦截所有Controller
public class CommonExceptionHandler {

    public ResponseEntity<ExceptionResule> handerException(LyException e){
        ExceptionEnum exceptionEnum = e.getExceptionEnum();

        return ResponseEntity.status(exceptionEnum.getCode()).body(new ExceptionResule(e.getExceptionEnum()));

    }




}
