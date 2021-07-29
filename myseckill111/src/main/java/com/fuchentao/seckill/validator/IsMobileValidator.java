package com.fuchentao.seckill.validator;

import com.fuchentao.seckill.util.ValidatorUtil;
import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class IsMobileValidator implements ConstraintValidator<IsMobile, String>{

    private boolean required = false;

    @Override
    public void initialize(IsMobile isMobile) {
        required = isMobile.required();
    }

    @Override
    public boolean isValid
            (String str, ConstraintValidatorContext constraintValidatorContext) {
        if (required) {
            return ValidatorUtil.isMobile(str);
        }
        else {
            if (StringUtils.isEmpty(str)) {
                return true;
            }
            else {
                return ValidatorUtil.isMobile(str);
            }
        }
    }

}
