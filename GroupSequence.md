# Introduction #

The GroupSequence annotation allows the developer to specify what groups should be run and in what order for a specific class.

# Example #

```
@GroupSequence({One.class,Two.class,Three.class})
public class Person {

    @NotNull(groups={One.class})
    @NotEmpty(groups={Two.class})
    @Length(groups={Three.class})
    private String name = null;

    
    @NotNull(groups={Two.class})
    @Past(groups={Two.class)
    private Date dob = null;

    public String getName() { return this.name; }
    public Date getDob() { return this.dob; }
}
```

When one group fails, the subsequent groups are not run.  This can allow the user to put more processing intensive tasks in later groups so that a minimum of effort is expended to find any invalid constraints.