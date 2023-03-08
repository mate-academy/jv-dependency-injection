# jv-dependency-injection

This is a simple file parser project.
In this project you can find three interfaces with their implementations.

Your task is to implement an injector that uses `@Component` and `@Inject` annotations to initialize and return 
Object instances. Convention is to use `Component` on the classes that are used for instance creation (usually these are interface implementations)
and `Inject` to mark fields that need to be initialized. If `Component` annotation is missing above the class, we shouldn't be able to create an instance of this class in Injector, and we should throw an exception <br>
NOTE: Pay attention to annotation's visibility when you want to use it with reflection API,
`@Retention` property may help you to configure it.

Это простой проект парсера файлов. В этом проекте вы можете найти три интерфейса с их реализациями.

Ваша задача — реализовать инжектор, который использует аннотации @Component и @Inject для инициализации и возврата экземпляров Object. 
Соглашение состоит в том, чтобы использовать Component для классов, которые используются для создания экземпляров (обычно это реализации интерфейса), 
и Inject для маркировки полей, которые необходимо инициализировать. Если аннотация Component отсутствует над классом, мы не сможем создать экземпляр этого 
класса в Injector, и мы должны выдать исключение.
Обратите внимание на видимость аннотации, если вы хотите использовать ее с API отражения, свойство @Retention может помочь вам настроить его.


[Try to avoid these common mistakes while solving task](https://mate-academy.github.io/jv-program-common-mistakes/java-core/dependency-injection/dependency-injection)
