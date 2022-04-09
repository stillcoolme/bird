package com.stillcoolme.designpattern.behavior.callback.first;

/**
 * Author: stillcoolme
 * Date: 2019/9/18 15:31
 * Description:
 *  https://juejin.im/post/5c913155f265da61200986a2
 * 在一个应用系统中，无论使用何种语言开发，必然存在模块之间的调用，调用的方式分为几种：
 * 1. 同步调用
 * 2. 异步调用
 *  1). 类A的方法方法a()通过新起线程的方式调用类B的方法b()，代码接着直接往下执行;
 *  2). 在方法a()需要方法b()执行结果的情况下，在Java中，可以使用Future+Callable的方式对方法b()的执行结果进行监听。
 * 3. 回调：
 *  1). 类A的a()方法调用类B的b(CallbackInterface interface)方法，然后A在自己类里面，实现CallbackInterface方法写 处理逻辑;
 *  2). 类B的b(CallbackInterface interface)方法，持有回调接口CallbackInterface，执行完毕主动调用类A的interface.callback()方法;
 *
 *  3） 异步回调：
 *  回调方提供可以异步回调的方法。askQuestion()里 new Thread(() -> student.answer(this)).start();
 *
 *
 *  下面代码代码模拟场景：老师问学生问题，学生思考完毕回答老师
 *  1. 首先定义一个回调接口Callback，回调方法 tellAnswer(int answer)，即学生思考完毕告诉老师答案
 *  2. 定义一个老师（回调方），实现Callback接口；老师有两个方法：
 *   （1）回调方法tellAnswer(int answer)，即学生回答完毕问题之后，老师要做的事情。这里回调方法参数是调用方传入的！！（定义好了接口，参数，就能先写回调业务逻辑，等学生回调的时候按情况处理，牛逼）
 *   （2）向问学生问题方法askQuestion()。这里面有 student.resolveQuestion(this)  将自身传入，用来进行回调！！
 *
 *  3. 定义一个学生接口，学生解决问题的方法 resolveQuestion(CallBack callback)，
 *      这个Callback参数，叫做注册回调接口，这样学生就知道解决完毕问题向谁报告
 *  4. 定义一个学生Tom（调用方），在解决完毕问题之后，向老师报告答案。
 *
 *  回调的核心就是
 *      实现回调接口的 回调方（老师）将本身即this传递给调用方（学生）! 然后调用方就可以在调用完毕之后执行回调方法 告诉 回调方 想要知道的信息。
 *  回调是一种思想、是一种机制，至于具体如何实现，如何通过代码将回调实现得优雅、实现得可扩展性比较高，
 *  一看开发者的个人水平，二看开发者对业务的理解程度。
 *
 *  如果觉得理解了，可以看看 zookeeper 是怎么把接收 Watcher 回调接口，进行 数据监听的
 *    https://blog.csdn.net/u010900754/article/details/78509779
 *    https://spongecaptain.cool/post/zookeeper/zookeeperwatch/#41-zookeeper-%E5%AE%A2%E6%88%B7%E7%AB%AF%E7%9A%84-watcher-%E6%B6%88%E6%81%AF%E5%8F%91%E9%80%81
 */
public class App {

    public static void main(String[] args) {

        Student student = new StudentTom();
        Teacher teacher = new Teacher(student);
        teacher.askQuestion();

    }

    /**
     * 分析一下上面的代码，上面的代码我这里做了两层的抽象：
     *
     * （1）将老师进行抽象
     *  将老师进行抽象之后，学生就不需要关心到底是哪位老师询问我问题，只要我根据询问的问题，得出答案，然后告诉提问的老师就可以了，
     *  即使老师换了一茬又一茬，对我学生而言都是没有任何影响的。
     *
     * （2）将学生进行抽象
     *  将学生进行抽象之后，对于老师这边来说就非常灵活，因为老师未必对一个学生进行提问，
     *  可能同时对Ricky、Jack、Lucy三个学生进行提问，这样就可以将成员变量Student改为List<Student>，
     *  这样在提问的时候遍历Student列表进行提问，然后得到每个学生的回答即可。
     *
     * 上面的例子，可能有人会提出这样的疑问：
     * 这个例子需要用什么回调啊，使用同步调用的方式，学生的 resolveQuestion() 方法 回答完毕问题之后直接 return结果给老师不就好了？。
     * 可以从两个角度去理解：
     *  首先，老师不仅仅想要得到学生的答案怎么办？
     *  可能这个老师在得到学生的答案之前，老师更想先知道学生姓名和学生的解题思路，
     *  当然有些人可以说，那我可以定义一个对象，里面加上学生的姓名和解题思路不就好了。
     *  这个说法在我看来有两个问题：
     * （1）如果老师想要的数据越来越多，那么返回的对象得越来越大，而使用回调则可以进行数据分离，
     *      将一批数据放在回调方法中进行处理，至于哪些数据依具体业务而定，如果需要增加返回参数，直接在回调方法中增加即可
     * （2）无法解决老师希望得到学生姓名、学生解题思路先于学生回答的答案
     * 因此我认为简单的返回某个结果确实没有必要使用回调而可以直接使用同步调用，
     * 但是如果有多种数据需要处理且数据有主次之分，使用回调会是一种更加合适的选择，优先处理的数据放在回调方法中先处理掉。
     */
    public void explain() {

    }
}
