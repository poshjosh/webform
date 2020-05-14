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
