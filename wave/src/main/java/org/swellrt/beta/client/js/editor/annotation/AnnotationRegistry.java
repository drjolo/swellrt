package org.swellrt.beta.client.js.editor.annotation;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.swellrt.beta.client.js.JsUtils;
import org.swellrt.beta.client.js.editor.SEditorException;
import org.waveprotocol.wave.client.common.util.JsoView;
import org.waveprotocol.wave.client.doodad.annotation.UserAnnotationHandler;
import org.waveprotocol.wave.client.doodad.link.LinkAnnotationHandler;
import org.waveprotocol.wave.client.editor.Editor;
import org.waveprotocol.wave.client.editor.content.ContentElement;
import org.waveprotocol.wave.client.editor.content.misc.AnnotationPaint;
import org.waveprotocol.wave.client.editor.content.misc.StyleAnnotationHandler;
import org.waveprotocol.wave.client.editor.content.paragraph.Paragraph;
import org.waveprotocol.wave.client.editor.content.paragraph.Paragraph.LineStyle;
import org.waveprotocol.wave.client.editor.content.paragraph.ParagraphBehaviour;
import org.waveprotocol.wave.model.conversation.AnnotationConstants;
import org.waveprotocol.wave.model.document.util.Range;
import org.waveprotocol.wave.model.util.CollectionUtils;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsonUtils;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsType;

/**
 * Putting together all annotation stuff to ease handling in the SwellRT editor
 * component.
 * <p>
 * <br>
 * List of text annotation handlers:
 * <p>
 * <li>{@link StyleAnnotationHandler}: handler for style annotations</li>
 * <li>{@link LinkAnnotationHandler}: handler for link annotations</li>
 * <li>{@link UserAnnotationHandler}: handler for custom annotations</li>
 * <p>
 * <br>
 * Rendering logic for these annotations are in following classes:
 * <p>
 * <li>{@link AnnotationPaint}: utility methods and constants for annotation
 * renderers</li>
 * <li>{@link AnnotationSpreadRenderer}: html renderer for style and custom
 * annotations</li>
 *
 * <p>
 * <br>
 * For Paragraph annotations check out {@link Paragraph} class.
 *
 * @author pablojan@gmail.com (Pablo Ojanguren)
 *
 */
@JsType(namespace = "swell.Editor", name = "AnnotationRegistry")
public class AnnotationRegistry {

  public static final String PARAGRAPH_HEADER = "paragraph/header";
  public static final String PARAGRAPH_TEXT_ALIGN = "paragraph/textAlign";
  public static final String PARAGRAPH_LIST = "paragraph/list";
  public static final String PARAGRAPH_INDENT = "paragraph/indent";

  public static final String STYLE_BG_COLOR = AnnotationConstants.STYLE_BG_COLOR;
  public static final String STYLE_COLOR = AnnotationConstants.STYLE_COLOR;
  public static final String STYLE_FONT_FAMILY = AnnotationConstants.STYLE_FONT_FAMILY;
  public static final String STYLE_FONT_SIZE = AnnotationConstants.STYLE_FONT_SIZE;
  public static final String STYLE_FONT_STYLE = AnnotationConstants.STYLE_FONT_STYLE;
  public static final String STYLE_FONT_WEIGHT = AnnotationConstants.STYLE_FONT_WEIGHT;
  public static final String STYLE_TEXT_DECORATION = AnnotationConstants.STYLE_TEXT_DECORATION;
  public static final String STYLE_VERTICAL_ALIGN = AnnotationConstants.STYLE_VERTICAL_ALIGN;

  public static final String LINK = AnnotationConstants.LINK_PREFIX;
  public static final String PARAGRAPH = "paragraph";

  private static final JsoView CANONICAL_KEYS = JsoView.create();

  private final static Map<String, AnnotationController> store = new HashMap<String, AnnotationController>();

  protected static boolean muteHandlers;

  /**
   * Define friendly names for annotation referencing in SEditor
   */
  static {

    //
    // Map annotation names without prefix to their canonical name
    // for the Wave system.
    //
    CANONICAL_KEYS.setString("header", PARAGRAPH_HEADER);
    CANONICAL_KEYS.setString("textAlign", PARAGRAPH_TEXT_ALIGN);
    CANONICAL_KEYS.setString("list", PARAGRAPH_LIST);
    CANONICAL_KEYS.setString("indent", PARAGRAPH_INDENT);

    CANONICAL_KEYS.setString("backgroundColor", STYLE_BG_COLOR);
    CANONICAL_KEYS.setString("color", STYLE_COLOR);
    CANONICAL_KEYS.setString("fontFamily", STYLE_FONT_FAMILY);
    CANONICAL_KEYS.setString("fontSize", STYLE_FONT_SIZE);
    CANONICAL_KEYS.setString("fontStyle", STYLE_FONT_STYLE);
    CANONICAL_KEYS.setString("fontWeight", STYLE_FONT_WEIGHT);
    CANONICAL_KEYS.setString("textDecoration", STYLE_TEXT_DECORATION);
    CANONICAL_KEYS.setString("verticalAlign", STYLE_VERTICAL_ALIGN);

    //
    // Paragraph Headers
    //

    Map<String, LineStyle> styles = new HashMap<String, LineStyle>();
    styles.put("h1", Paragraph.regularStyle("h1"));
    styles.put("h2", Paragraph.regularStyle("h2"));
    styles.put("h3", Paragraph.regularStyle("h3"));
    styles.put("h4", Paragraph.regularStyle("h4"));
    styles.put("h5", Paragraph.regularStyle("h5"));
    styles.put("default", Paragraph.regularStyle(""));

    store.put(PARAGRAPH_HEADER, new AnnotationController(ParagraphBehaviour.HEADING,
        PARAGRAPH_HEADER, styles, new AnnotationController.AttributeGenerator() {

      @Override
      public Map<String, String> generate(Range range, String styleKey) {
        // This code auto generates an id for each header! so we can reference them in the DOM
        Date now = new Date();
        String id = String.valueOf(now.getTime()) +
                    String.valueOf(range.getStart()) +
                    String.valueOf(range.getEnd());
        return CollectionUtils.<String, String> immutableMap(Paragraph.ID_ATTR, id);
      }
        }));

    //
    // Paragraph Text alignment
    //

    styles = new HashMap<String, LineStyle>();
    styles.put("left", Paragraph.Alignment.LEFT);
    styles.put("center", Paragraph.Alignment.CENTER);
    styles.put("right", Paragraph.Alignment.RIGHT);
    styles.put("justify", Paragraph.Alignment.JUSTIFY);
    styles.put("default", Paragraph.Alignment.LEFT);

    store.put(PARAGRAPH_TEXT_ALIGN,
        new AnnotationController(ParagraphBehaviour.DEFAULT, PARAGRAPH_TEXT_ALIGN, styles, null));

    //
    // Paragraph List
    //

    styles = new HashMap<String, LineStyle>();
    styles.put("decimal", Paragraph.listStyle(Paragraph.LIST_STYLE_DECIMAL));
    styles.put("unordered", Paragraph.listStyle(null));
    styles.put("default", Paragraph.listStyle(null));

    store.put(PARAGRAPH_LIST,
        new AnnotationController(ParagraphBehaviour.LIST, PARAGRAPH_LIST, styles, null));

    //
    // Paragraph indentation
    //

    Map<String, ContentElement.Action> ma = new HashMap<String, ContentElement.Action>();
    ma.put("outdent", Paragraph.OUTDENTER);
    ma.put("indent", Paragraph.INDENTER);

    store.put(PARAGRAPH_INDENT,
        new AnnotationController(PARAGRAPH_INDENT, ma, Paragraph.RESET_INDENT));

    //
    // Style Annotations
    //

    AnnotationController styleController = new AnnotationController(
        AnnotationConstants.STYLE_PREFIX);
    store.put(AnnotationConstants.STYLE_PREFIX, styleController);

    AnnotationPaint.registerEventHandler(AnnotationConstants.STYLE_PREFIX,
        styleController.getTextEventHanlder());
    AnnotationPaint.setMutationHandler(AnnotationConstants.STYLE_PREFIX,
        styleController.getTextEventHanlder());

    //
    // Link annotation
    //

    AnnotationController linkController = new AnnotationController(AnnotationConstants.LINK_PREFIX);
    store.put(AnnotationConstants.LINK_PREFIX, new AnnotationController(AnnotationConstants.LINK_PREFIX));

    AnnotationPaint.registerEventHandler(AnnotationConstants.LINK_PREFIX,
        linkController.getTextEventHanlder());
    AnnotationPaint.setMutationHandler(AnnotationConstants.LINK_PREFIX,
        linkController.getTextEventHanlder());
  }


  @JsIgnore
  public static Set<String> getKeys() {
    return store.keySet();
  }


  /**
   * Retrieve a {@link Annotation} instance.
   *
   *
   * @param name,
   *          maybe including a suffix
   * @return
   */
  @JsIgnore
  public static AnnotationController get(String key) {
    if (key.contains("/") &&
        !key.startsWith(LINK) && !key.startsWith(PARAGRAPH)) {
      key = key.substring(0, key.indexOf("/"));
    }
    String canonicalName = CANONICAL_KEYS.getString(key);
    return store.get(canonicalName != null ? canonicalName : key);
  }


  public static boolean isParagraphAnnotation(String key) {
    String canonicalName = CANONICAL_KEYS.getString(key);
    key = canonicalName != null ? canonicalName : key;
    return key.startsWith("paragraph/");
  }

  /**
   * Define a new custom annotation.
   *
   * @param key annotation's name
   * @param cssClass a css class for the html container
   * @param cssStyle css styles for the html container
   */
  public static void define(String key, String cssClass, JavaScriptObject cssStyleObj) throws SEditorException {

    if (key == null || key.startsWith("paragraph") || key.startsWith(AnnotationConstants.STYLE_PREFIX) || CANONICAL_KEYS.getString(key) != null) {
      throw new SEditorException("Not valid annotation name");
    }

    JsoView cssStyles = null;
    if (JsUtils.isString(cssStyleObj)) {
      JavaScriptObject o = JsonUtils.unsafeEval(cssStyleObj.toString());
      if (o != null) {
        cssStyles = JsoView.as(o);
      }
    } else {
      cssStyles = JsoView.as(cssStyleObj);
    }

    AnnotationController annotation = new AnnotationController(key);
    store.put(key, annotation);
    UserAnnotationHandler.register(Editor.ROOT_REGISTRIES, key, cssClass, cssStyles,
        annotation.getTextEventHanlder(), annotation.getTextEventHanlder(),
        annotation.getTextEventHanlder());
  }

  /**
   * Set a handler for events on one annotation type
   * @param key
   * @param handler
   */
  public static void setHandler(String key, AnnotationEvent.Handler handler) {
    AnnotationController ac = get(key);

    if (ac != null) {
      ac.setEventHandler(handler);
    }
  }

  /**
   * Clear the handler for events on one annotation type
   * @param name
   * @param handler
   */
  public static void unsetHandler(String name) {
    AnnotationController ac = get(name);

    if (ac != null) {
      ac.unsetEventHandler();
    }
  }


  public static void muteHandlers(boolean b) {
    AnnotationRegistry.muteHandlers = b;
  }

}
