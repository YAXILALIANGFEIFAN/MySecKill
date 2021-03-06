package com.fuchentao.seckill.redis;

/*
模板模式，先定义接口，再定义抽象类，再定义实现类

接口和抽象类的异同
相同点：
1、都不能被实例化。
2、接口的实现类和抽象类的子类只有全部实现了接口或者抽象类中的方法后才可以被实例化。

不同点：
1、接口只能定义抽象方法不能实现方法，抽象类既可以定义抽象方法，也可以实现方法。
2、单继承，多实现。接口可以实现多个，只能继承一个抽象类。
3、接口强调的是功能，抽象类强调的是所属关系。
4、接口中的所有成员变量 为public static final， 静态不可修改，当然必须初始化。
   接口中的所有方法都是public abstract 公开抽象的。而且不能有构造方法。
   抽象类就比较自由了，和普通的类差不多，可以有抽象方法也可以没有，
   可以有正常的方法，也可以没有。


*/
public interface KeyPrefix {

    //设置一个有效期和key的前缀
    public int expireSeconds();
    public String getPrefix();
}
