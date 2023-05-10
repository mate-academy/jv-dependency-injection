# jv-dependency-injection

This is a simple file parser project.
In this project you can find three interfaces with their implementations.

Your task is to implement an injector that uses `@Component` and `@Inject` annotations to initialize and return 
Object instances. Convention is to use `Component` on the classes that are used
for instance creation (usually these are interface implementations)
and `Inject` to mark fields that need to be initialized. If `Component`
annotation is missing above the class, we shouldn't be able to create an instance of this class in Injector,
and we should throw an exception <br>
NOTE: Pay attention to annotation's visibility when you want to use it with reflection API,
`@Retention` property may help you to configure it.


Це простий проект аналізатора файлів. У цьому проекті ви можете знайти три інтерфейси з їх реалізаціями. 
Ваше завдання — реалізувати інжектор, який використовує анотації @Component і @Inject для ініціалізації та 
повернення екземплярів Object. 
Загальноприйнято використовувати `Component` для класів, які використовуються
для створення екземплярів (зазвичай це реалізації інтерфейсу), і `Inject` для позначення полів, які потрібно 
ініціалізувати. Якщо над класом відсутня анотація `Component`, ми не зможемо створити екземпляр цього класу в Injector,
і ми повинні створити виняток <br> ПРИМІТКА: Зверніть увагу на видимість анотації, 
якщо ви хочете використовувати її з API відображення, властивість @Retention може допомогти вам налаштувати його.
[Try to avoid these common mistakes while solving task](https://mate-academy.github.io/jv-program-common-mistakes/java-core/dependency-injection/dependency-injection)
