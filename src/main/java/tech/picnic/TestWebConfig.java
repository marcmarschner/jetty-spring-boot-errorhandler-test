package tech.picnic;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.collect.ImmutableList;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

public class TestWebConfig extends WebMvcConfigurerAdapter {

  /** Always reply JSON, unless no conversion to JSON is available */
  @Override
  public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
    configurer.defaultContentTypeStrategy(
        unused -> ImmutableList.of(MediaType.APPLICATION_JSON, MediaType.ALL));
  }

  @Override
  public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
    converters.add(converter(objectMapper()));
    converters.add(new ByteArrayHttpMessageConverter());
    StringHttpMessageConverter stringHttpMessageConverter =
        new StringHttpMessageConverter(StandardCharsets.UTF_8);
    stringHttpMessageConverter.setWriteAcceptCharset(false);
    converters.add(stringHttpMessageConverter);
    super.configureMessageConverters(converters);
  }

  private ObjectMapper objectMapper() {
    return new ObjectMapper()
        .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
        .setVisibility(PropertyAccessor.CREATOR, JsonAutoDetect.Visibility.ANY)
        .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
        .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)
        .enable(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS)
        .enable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY)
        .enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE)
        .enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .disable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
        .disable(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
  }

  private MappingJackson2HttpMessageConverter converter(ObjectMapper objectMapper) {
    MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
    converter.setObjectMapper(objectMapper);
    return converter;
  }
}
