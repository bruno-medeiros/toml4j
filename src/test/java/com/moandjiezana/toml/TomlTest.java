package com.moandjiezana.toml;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Calendar;
import java.util.TimeZone;

import org.hamcrest.Matchers;
import org.junit.Test;

public class TomlTest {
  @Test
  public void should_get_table() throws Exception {
    Toml toml = new Toml().parse("[group]\nkey = \"value\"");

    Toml group = toml.getTable("group");

    assertEquals("value", group.getString("key"));
  }

  @Test
  public void should_get_value_for_multi_key() throws Exception {
    Toml toml = new Toml().parse("[group]\nkey = \"value\"");

    assertEquals("value", toml.getString("group.key"));
  }

  @Test
  public void should_get_value_for_multi_key_with_no_parent_table() throws Exception {
    Toml toml = new Toml().parse("[group.sub]\nkey = \"value\"");

    assertEquals("value", toml.getString("group.sub.key"));
  }

  @Test
  public void should_get_table_for_multi_key() throws Exception {
    Toml toml = new Toml().parse("[group]\nother=1\n[group.sub]\nkey = \"value\"");

    assertEquals("value", toml.getTable("group.sub").getString("key"));
  }

  @Test
  public void should_get_table_for_multi_key_with_no_parent_table() throws Exception {
    Toml toml = new Toml().parse("[group.sub]\nkey = \"value\"");

    assertEquals("value", toml.getTable("group.sub").getString("key"));
  }

  @Test
  public void should_get_value_from_table_with_sub_table() throws Exception {
    Toml toml = new Toml().parse("[a.b]\nc=1\n[a]\nd=2");

    assertEquals(2, toml.getLong("a.d").intValue());
    assertEquals(1, toml.getTable("a.b").getLong("c").intValue());
  }
  
  @Test
  public void should_handle_navigation_to_missing_value() throws Exception {
    Toml toml = new Toml();
    
    assertNull(toml.getString("a.b"));
    assertNull(toml.getString("a.b[0].c"));
    assertThat(toml.getList("a.b"), hasSize(0));
    assertTrue(toml.getTable("a.b").isEmpty());
    assertTrue(toml.getTable("a.b[0]").isEmpty());
  }

  @Test
  public void should_return_null_if_no_value_for_key() throws Exception {
    Toml toml = new Toml().parse("");

    assertNull(toml.getString("a"));
  }

  @Test
  public void should_return_empty_list_if_no_value_for_key() throws Exception {
    Toml toml = new Toml().parse("");

    assertTrue(toml.<String>getList("a").isEmpty());
  }

  @Test
  public void should_return_null_when_no_value_for_multi_key() throws Exception {
    Toml toml = new Toml().parse("");

    assertNull(toml.getString("group.key"));
  }

  @Test
  public void should_load_from_file() throws Exception {
    Toml toml = new Toml().parse(new File(getClass().getResource("should_load_from_file.toml").getFile()));

    assertEquals("value", toml.getString("key"));
  }

  @Test
  public void should_support_blank_lines() throws Exception {
    Toml toml = new Toml().parse(new File(getClass().getResource("should_support_blank_line.toml").getFile()));

    assertEquals(1, toml.getLong("group.key").intValue());
  }

  @Test
  public void should_allow_comment_after_values() throws Exception {
    Toml toml = new Toml().parse(new File(getClass().getResource("should_allow_comment_after_values.toml").getFile()));

    assertEquals(1, toml.getLong("a").intValue());
    assertEquals(1.1, toml.getDouble("b").doubleValue(), 0);
    assertEquals("abc", toml.getString("c"));
    Calendar cal = Calendar.getInstance();
    cal.set(2014, Calendar.AUGUST, 4, 13, 47, 0);
    cal.set(Calendar.MILLISECOND, 0);
    cal.setTimeZone(TimeZone.getTimeZone("UTC"));
    assertEquals(cal.getTime(), toml.getDate("d"));
    assertThat(toml.<String>getList("e"), Matchers.contains("a", "b"));
    assertTrue(toml.getBoolean("f"));
    assertEquals("abc", toml.getString("g"));
    assertEquals("abc", toml.getString("h"));
    assertEquals("abc\nabc", toml.getString("i"));
    assertEquals("abc\nabc", toml.getString("j"));
  }
  
  @Test
  public void should_be_empty_if_no_values() throws Exception {
    assertTrue(new Toml().isEmpty());
    Toml toml = new Toml().parse("[a]");
    assertTrue(toml.getTable("a").isEmpty());
    assertTrue(toml.getTable("b").isEmpty());
    assertFalse(toml.isEmpty());
  }

  @Test(expected = IllegalStateException.class)
  public void should_fail_on_empty_key_name() throws Exception {
    new Toml().parse(" = 1");
  }
  
  @Test(expected = IllegalStateException.class)
  public void should_fail_on_key_name_with_hash() throws Exception {
    new Toml().parse("a# = 1");
  }
  
  @Test(expected = IllegalStateException.class)
  public void should_fail_on_key_name_starting_with_square_bracket() throws Exception {
    new Toml().parse("[a = 1");
  }

  @Test(expected = IllegalStateException.class)
  public void should_fail_when_key_is_overwritten_by_table() {
    new Toml().parse("[a]\nb=1\n[a.b]\nc=2");
  }

  @Test(expected = IllegalStateException.class)
  public void should_fail_when_key_is_overwritten_by_another_key() {
    new Toml().parse("[fruit]\ntype=\"apple\"\ntype=\"orange\"");
  }

  @Test(expected = IllegalStateException.class)
  public void should_fail_when_table_defined_twice() throws Exception {
    new Toml().parse("[a]\nb=1\n[a]\nc=2");
  }

  @Test(expected = IllegalStateException.class)
  public void should_fail_when_illegal_characters_after_table() throws Exception {
    new Toml().parse("[error]   if you didn't catch this, your parser is broken");
  }
}
