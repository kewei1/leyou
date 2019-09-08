package com.leyou.common.exception;

import com.leyou.common.enuums.ExceptionEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@AllArgsConstructor
@Getter
public class LyException extends RuntimeException{
    private ExceptionEnum exceptionEnum;

}
