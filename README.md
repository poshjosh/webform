# webform

### Auto generate HTML forms for your web application

HTML forms are generated based on domain object annotations, including:

    - javax.persistence.Basic
    - javax.persistence.Column
    - javax.persistence.Entity
    - javax.persistence.Enumerated
    - javax.persistence.GeneratedValue
    - javax.persistence.GenerationType
    - javax.persistence.Id
    - javax.persistence.ManyToMany
    - javax.persistence.ManyToOne
    - javax.persistence.OneToMany
    - javax.persistence.OneToOne
    - javax.persistence.Table
    - javax.validation.constraints.NotNull
    - javax.validation.constraints.Size;

Enums are treated as HTML `<select>` element.

### Handling DTO

To use DTOs, you need to register mappers for each DTO/Entity mapping

```java
@Configuration
public class WebformConfigurerImpl implements WebformConfigurer{
    
    @Autowired private AuctionItemMapper auctionItemMapper;
    
    @Override
    public void addEntityMappers(EntityMapperService mapperService) {
        mapperService.setMapper(
                AuctionItemDTO.class, AuctionItem.class, auctionItemMapper));
    }
}
```

You also need to use the DTO in place of entity type in `webform.properties`

```
webform.field.value.default.AuctionItemDTO.priceIncrement=1
webform.field.value.default.AuctionItemDTO.status=0
webform.field.type.AuctionItemDTO.status=hidden
```

### Custom values for form fields

To add some default/custom values to form fields:

1. Using properties file. Here we are setting the default value of 
the properties `AuctionItemDTO.priceIncrement` and `AuctionItemDTO.status`

```
webform.field.value.default.AuctionItemDTO.priceIncrement=1
webform.field.value.default.AuctionItemDTO.status=0
```

You could use the full class name, simple class name or just property name.

For example given entity `org.domain.Person` and property `dateTimeFormat`, the
value of the property is resolved in the follow order. The first match is 
selected.

```
form.dateTimeFormat.org.domain.Person.dateOfBirth
 
form.dateTimeFormat.Person.dateOfBirth
 
form.dateTimeFormat.org.domain.Person
 
form.dateTimeFormat.Person
 
form.dateTimeFormat.dateOfBirth

form.dateTimeFormat
```

2. Add a Configurer

```java
@Configuration
public class WebformConfigurerImpl implements WebformConfigurer{
    
    @Override
    public void addEntityConfigurers(EntityConfigurerService configurerService) {
        configurerService.addConfigurer(AccountDetails.class, new AccountDetailsConfigurer());
        configurerService.addConfigurer(Address.class, new AddressConfigurer());
        configurerService.addConfigurer(Auction.class, new AuctionConfigurer());
    }
}

```

And here is a sample of a Configurer:

```java
public class AddressConfigurer implements EntityConfigurer<Address>{

    @Override
    public Address configure(Address address, FormRequest<Address> formRequest) {
        
        // Configure the newly created Address entity here
    
        return address;
    }
}
```

### Uploading/Deleting Images

Uploading/deleting of images are handled by class `FileUploadHandler` which 
is a bean that becomes available when you provide and instance of
`com.looseboxes.webform.config.WebformFileuploadConfigurationSource` for 
example as shown below:

```java
public class WebformFileuploadConfiguration extends WebformFileuploadConfigurationSource{

   ... // Any additional spring beans could be added here    
}

```




