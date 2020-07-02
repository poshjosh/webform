package com.looseboxes.webform.util;

import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import org.junit.jupiter.api.Test;

/**
 * @author hp
 */
public class ObjectGraphAsListBuilderTest {
    
    // @TODO 
    // - Handle circular reference
    // - Handle duplicates
    
    @Target(TYPE)
    @Retention(RUNTIME)
    public static @interface TypeToAccept { }
    
    enum Country{ Nigeria, Bolivia, Germany, Austrailia }
    enum UserStatus{ Unactivated, Activated }
    enum GroupStatus{ Inactive, Active }
    
    @TypeToAccept static class Region{
        private String name;
        private Country country;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public Country getCountry() { return country; }
        public void setCountry(Country country) { this.country = country; }
    }
    
    @TypeToAccept public static class Address{
        String street;
        Region region;
    }
    
    @TypeToAccept public static class UserProfile{
        UserStatus status;
        Address address;
    }
    
    @TypeToAccept public static class UserGroup{
        UserProfile manager;
        GroupStatus status;
//        UserGroup parentGroup; @TODO - Handle circular reference
    }
    
    private final int levelDepth = 4;
    
    @Test
    public void givenMaxDepthLessThanMinusOne_shouldThrowRuntimeException() {
        System.out.println("givenMaxDepthLessThanMinusOne_shouldThrowRuntimeException");
        try{
            this.getObjectGraphAsListBuilder(-10);
            fail("Should throw RuntimeException, but completed execution");
        }catch(RuntimeException expected) { }
    }

    @Test
    public void givenMaxDepthOfMinusOne_shouldReturnValidList() {
        this.givenMaxDepth_shouldReturnListOfSizeAndValidContent(-1, this.levelDepth);
    }

    @Test
    public void givenMaxDepthOfZero_shouldReturnValidList() {
        this.givenMaxDepth_shouldReturnListOfValidSizeAndContent(0);
    }

    @Test
    public void givenMaxDepthOfOne_shouldReturnValidList() {
        this.givenMaxDepth_shouldReturnListOfValidSizeAndContent(1);
    }
    
    @Test
    public void givenMaxDepthOfTwo_shouldReturnValidList() {
        this.givenMaxDepth_shouldReturnListOfValidSizeAndContent(2);
    }

    @Test
    public void givenMaxDepthOfThree_shouldReturnValidList() {
        this.givenMaxDepth_shouldReturnListOfValidSizeAndContent(3);
    }

    @Test
    public void givenMaxDepthFour_shouldReturnValidList() {
        this.givenMaxDepth_shouldReturnListOfSizeAndValidContent(4, this.levelDepth);
    }

    @Test
    public void givenVeryLargeMaxDepth_shouldReturnValidList() {
        this.givenMaxDepth_shouldReturnListOfSizeAndValidContent(Integer.MAX_VALUE, this.levelDepth);
    }

    @Test
    public void givenEntityWithNullValues_shouldReturnListOfSizeOne() {
        System.out.println("givenEntityWithNullValues_shouldReturnListOfSizeOne");
        this.shouldReturnListOfSize(1, 
                this.givenMaxDepthOf(-1),
                this.givenEntityWithNullValues(),
                this.withTestToAcceptEntities());
    }

    private void givenMaxDepth_shouldReturnListOfValidSizeAndContent(int depth) {
        // We add one because of the root element, which is the last element in the returned list
        this.givenMaxDepth_shouldReturnListOfSizeAndValidContent(depth, depth + 1);
    }
    
    private void givenMaxDepth_shouldReturnListOfSizeAndValidContent(int depth, int outputSize) {
//        System.out.println("givenMaxDepthOf("+depth+")_shouldReturnListOfSize(" + outputSize + ")_andValidContent");
        System.out.println("givenMaxDepthOf("+depth+")_shouldReturnValidList");
        this.shouldReturnListOfSize(outputSize, 
                this.givenMaxDepthOf(depth),
                this.givenNestedEntity(),
                this.withTestToAcceptEntities());
    }

    private ObjectGraphAsListBuilder givenMaxDepthOf(int maxDepth) {
        ObjectGraphAsListBuilder instance = this.getObjectGraphAsListBuilder(maxDepth);
        return instance;
    }

    private void shouldReturnListOfSize(
            int expectedOutputSize, ObjectGraphAsListBuilder builder, 
            Object bean, BiPredicate test) {
        if(expectedOutputSize > levelDepth) {
            expectedOutputSize = levelDepth;
        }
        final List result = builder.build(bean, test);
        System.out.println("  Result: " + result);
        final List expResult = this.expectedResult(bean, expectedOutputSize);
        System.out.println("Expected: " + expResult);
        assertThat(result.size(), is(expectedOutputSize));
        assertThat(result, is(expResult));
    }

    private BiPredicate<Field, Object> withTestToAcceptEntities() {
        return (field, fieldValue) -> field.getType().getAnnotation(TypeToAccept.class) != null;
    }
    
    private UserGroup givenEntityWithNullValues() {
        return new UserGroup();
    }
    
    private List expectedResult(Object bean, int expectedOutputSize) {
        if(expectedOutputSize > levelDepth) {
            expectedOutputSize = levelDepth;
        }
        List expected = new ArrayList();
        UserGroup grp = (UserGroup)bean;
        if(grp != null) {
            expected.add(grp);
            UserProfile usr = grp.manager;
            if(usr != null) {
                expected.add(usr);
                Address adr = usr.address;
                if(adr != null) {
                    expected.add(adr);
                    Region rgn = adr.region;
                    if(rgn != null) {
                        expected.add(rgn);
                    }
                }
            }
        }
        List result = expected.subList(0, expectedOutputSize);
        Collections.reverse(result);
        return result;
    }
    
    private UserGroup givenNestedEntity() {

        Region region = new Region();
        region.country = Country.Nigeria;
        region.name = "Abuja";

        Address address = new Address();
        address.region = region;
        address.street = "Plot 551 Cadastral Zone B06";
        
        UserProfile userProfile = new UserProfile();
        userProfile.address = address;
        userProfile.status = UserStatus.Activated;
        
        UserGroup userGroup = new UserGroup();
        userGroup.manager = userProfile;
        userGroup.status = GroupStatus.Active;
        
        return userGroup;
    }
    
    private ObjectGraphAsListBuilder getObjectGraphAsListBuilder(int depth) {
        return new ObjectAsGraphListBuilderImpl(depth);
    }
}
