# InBank Backend Service - Validation of TICKET 101

## What the intern did well?
    - Using layered architecture
    - Adding comments to each method to explain what they do
    - Writing tests
    - Simple validation of methods
    - Using DTOs

## What I changed or could be improved?

### Constants
    - Constant file is in the config folder. Config folder is used for configurations, which goes against the clean code principles (Uncle Bob:).
    - Keeping all the configs in one file in a "public static final" way could be used, but this approach has some drawbacks.
      Firstly, we may not want fields to be reachable from all the classes. Instead, it would be better to configure them to read from the `application.properties` (or `application.yml`) file.
      This approach avoids hardcoded parts, meaning when changes are needed, we only have to change configurations in the `application.yml` (.properties), which also follows the SOLID principles (O - Open to changes but closed to modification).
      Moreover, this approach makes our system loosely coupled, and sometimes we need to define variable values based on the environment, such as test, dev, prod... For production readiness, I would recommend using tools like HashiCorp Vault or Kubernetes Secrets.

### DTOs
    - The `DecisionRequest` and `DecisionResponse` are our DTOs, which should be in the `dto` folder, considering the layered architecture implemented for this project.
    - For the DTOs, another approach could be using Java record classes, which bring immutability.
    - The `Decision` class is in the wrong folder. It should be part of the DTOs folder. During development, I realized we don't need the `Decision` class. Instead, `DecisionResponse` could be used for the same purpose, so I deleted the `Decision` class.
    - `DecisionResponse` and `DecisionRequest` fields are used in different data structures, or one uses primitive types while the other uses wrapper classes. This is not a good practice. For now, I kept them the same because it would affect the client side, and I tried to make minimal changes.
    - No need to use `@Component` annotation in `DecisionResponse`.
    - Based on the ticket, "decision engine returns a decision (negative or positive) and the amount," which is not implemented in the code and is not suitable for the client side either. I’ll leave it as it is for now.

### CONTROLLER
    - I changed the folder name to `controller`, which is more convenient for layered architecture.
    - Adding a version to the URL would be a best practice for the future.
    - The controller shouldn't contain the business logic.

### EXCEPTIONS
    - Extending exception classes from `Throwable` works, but it's abstract. Instead, it would be better to extend from the `Exception` or `RuntimeException` class.
      `Throwable` (and its subclasses) already have message and cause fields, so manually declaring them is unnecessary. Instead, use `super(message, cause)` in constructors.
      Since `Exception` already provides `getMessage()` and `getCause()`, overriding them is unnecessary.
    
      Additionally, a general exception advisor could be used to display a custom error page.
    
      Brief info about these classes:
        - `Throwable` is the superclass of `Error` and `Exception`. It generally doesn't make sense to use it in our custom exceptions because `Error` and `Exception` already extend from `Throwable`.
        - `Error` - something we can't handle at all: `OutOfMemoryError`, `VirtualMachineError`, etc.
        - `Exception` - Contains both `RuntimeException` and checked exceptions.

### Service
      - Exceptions could be handled in a try-catch block.
      - Some of the method logic could be divided into other methods.
      - In the `verifyInputs()` method, exception messages are hardcoded. Instead, they could be kept as constants or in an enum file. 
        P.S. Considering we’re not using a database and don’t support multilanguage functionality.

### Overall
    - Lack of naming
    - Architectural problems
    - Logging
    - Lombok is used in some classes, but sometimes constructor injection is used, which makes the code unclear. If Lombok usage is approved, I would recommend using it in all classes.
    - `getCreditModifier` segments are hardcoded, I made them dynamic.
    - Test lacks naming.
    - Testing multiple scenarios in one unit test.
    - Ticket isn’t fully implemented.

### General Notes
    - Assuming all the `fin` codes are in the same format, I used just the `EstonianPersonalCodeValidator` class.
    - For test purposes, the Lithuania age limit is set to 40 to 70.
    - UI mistake: the month number starts at 6 instead of 12, which I changed.
    - UI doesn’t show the approved loan amount appropriately; it only shows the loan amount of the customer’s choice. This is another change needed on the client side.
    - Based on the ticket, the maximum loan period could be 48, so I changed it from 60 to 48.
    - I’ve added simple logs to the project. It could be improved by keeping them in one folder, assigning log levels, and other properties could be improved.
    - Multilingual functionality could be added.
    - DTO validation: For now, I didn’t implement it, but it could be added.
