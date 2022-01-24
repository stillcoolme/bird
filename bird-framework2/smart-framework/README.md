
1. 如何加载并读取配置文件;

2. 如何加载指定的类;
    * 开发一个“类加载器”来加载该基础包名下的所有类，比如使用了某注解的类、实现了某接口的类、继承了某父类的所有子类等。
    有必要写ClassUtil工具类，提供与类操作相关的方法，比如获取类加载器、加载类、获取指定包名下的所有类等。
    * 然后ClassHelper通过这个来加载应用包下的所有类，并能返回各种注解的类集合；
    * 构造Bean容器，BeanHelper 用反射工具类 RefectionUtil 来将所有类实例化，将类名与实例的映射关系记录到map中；

3. 如何实现一个简单的IOC容器;
    * 实现 IocHelper，拿 BeanHelper 得到的 BeanMap 来遍历，将里面实例中带有 Inject注解 的 成员变量 通过 ReflectionUtil#setField方法将成员变量设置到实例里。
        （注意，Ioc底层还是从 BeanHelper 中获取BeanMap的，这些对象 BeanHelper 都是单例创建的）
    
    * 实现 ControllerHelper， 通过ClassHelper，我们可以获取所有定义了 Controller 注解的类，再获取加了 Action 注解的方法，进而
      获取 Action注解中写的请求方法与请求路径，封装请求对象(Request)与处理对象(Handler)并建立一个映射关系，放入一个Action Map中，并提供一个可根据请求方法与请求路径获取处理对象的方法。
    
    * 几个Helper类需要通过一个入口程序来加载它们，先集中到 HelperLoader中加载。  
    
4. 如何初始化框架，接收请求，返回数据。

    * 编写一个DispatcherServlet，让它来处理所有的请求。
        从HttpServletRequest对象中获取请求方法与请求路径，再通过ControllerHelpert#getHandler拿到处理请求的Handler对象，进而通过BeanHelper.getBean方法获取Controller的实例对象。
        从HttpServletRequest对象中获取所有请求参数，并将其初始化到一个名为Param对象中。
    * 返回对象。Handler对象里面有 Action注解 对应的方法，所以可以反射获取 Action注解 的方法的返回值。
        （1）如果返回对象是 View 类型对象，则返回一个JSP页面；（2）如果返回值是 Data 类型对象，则返回一个 json。
    返回的Data类型的数据封装了一个Object类型的模型数据，框架会将该对象写入HttpServletResponse对象中，从而直接输出至浏览器。
    
5. AOP

在AOP中，我们需要定义一个Aspect (切面)类来编写需要横切业务逻辑的代码。此外，还需要通过一个条件来匹配想要拦截的类，在AOP中称为Pointcut (切点)。













