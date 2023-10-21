# jv-dependency-injection

This is a simple file parser project.
In this project, you can find three interfaces with their implementations.

Your task is to implement an injector that uses `@Component` and `@Inject` annotations to initialize and return 
Object instances. The convention is to use `Component` on the classes that are used for instance creation (usually these are interface implementations)
and `Inject` to mark fields that need to be initialized. If the `Component` annotation is missing above the class, we shouldn't be able to create an instance of this class in Injector, and we should throw an exception <br>
NOTE: Pay attention to the annotation's visibility when you want to use it with reflection API, the `@Retention` property may help you configure it.

[Try to avoid these common mistakes while solving task](./checklist.md)
