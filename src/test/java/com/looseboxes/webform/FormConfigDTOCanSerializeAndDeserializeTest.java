package com.looseboxes.webform;

import com.bc.webform.TypeTestsImpl;
import com.bc.webform.choices.SelectOptionBean;
import com.bc.webform.form.DefaultForm;
import com.bc.webform.form.FormBean;
import com.bc.webform.form.member.DefaultFormMember;
import com.bc.webform.form.member.FormMemberBean;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.looseboxes.webform.json.WebformJsonOutputConfigurer;
import com.looseboxes.webform.web.FormConfigDTO;
import java.util.Collections;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * @author hp
 */
public class FormConfigDTOCanSerializeAndDeserializeTest {
    
    public static class Blog{
        private String title;
        public String getTitle() {
            return title;
        }
        public void setTitle(String title) {
            this.title = title;
        }
    }
    
    private static ObjectMapper objectMapper;
    
    @BeforeAll
    public static void beforeAll() {
        objectMapper = new WebformJsonOutputConfigurer(new TypeTestsImpl()).createConfiguredObjectMapper();
    }

    @Test
    public void testSerializeThenDeserialize() {
        
        final String name = "blog";
    
        FormConfigDTO dto = new FormConfigDTO();
        dto.setCrudAction(CRUDAction.create);
        dto.setFid(Long.toHexString(System.currentTimeMillis()));
        dto.setModelname(name);
        dto.setUploadedFiles(Collections.singletonList(System.getProperty("user.home")));

        FormBean form = new DefaultForm(name);
        dto.setForm(form);
        
        Blog dataSource = new Blog();
        dataSource.setTitle("My First Blog");
        form.setDataSource(dataSource);
        
        FormMemberBean member = new DefaultFormMember(form, "title");
        member.setForm(null); // Prevent recursion
        
        form.setMembers(Collections.singletonList(member));
        member.setAdvice("The title of the blog");
        try{
            member.setDataSource(Blog.class.getDeclaredField("title"));
        }catch(NoSuchFieldException | SecurityException e) {
            throw new RuntimeException(e);
        }
        member.setMultiChoice(Boolean.TRUE);

        SelectOptionBean choice = new SelectOptionBean();
        member.setChoices(Collections.singletonList(choice));
        choice.setText("1 - My First Blog");
        choice.setValue("My First Blog");
        
        try{
            
            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dto);
            System.out.println(json);
            
            FormConfigDTO read = objectMapper.readValue(json, FormConfigDTO.class);
            System.out.println(read);
            
        }catch(JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
