package org.swellrt.beta.client.js.editor.annotation;

import org.waveprotocol.wave.client.editor.content.ContentElement;
import org.waveprotocol.wave.client.editor.content.paragraph.Paragraph;
import org.waveprotocol.wave.client.editor.content.paragraph.Paragraph.Alignment;
import org.waveprotocol.wave.client.editor.content.paragraph.Paragraph.EventHandler;
import org.waveprotocol.wave.client.editor.content.paragraph.Paragraph.MutationHandler;
import org.waveprotocol.wave.client.editor.content.paragraph.ParagraphBehaviour;

import com.google.gwt.user.client.Event;

public class ParagraphEventHandler implements EventHandler, MutationHandler {

  private final String key;
  private final ParagraphBehaviour behaviour;

  private AnnotationEvent.Handler handler;

  public ParagraphEventHandler(String key, ParagraphBehaviour behaviour) {
    super();
    this.key = key;
    this.behaviour = behaviour;
  }

  public void register() {
    Paragraph.registerEventHandler(behaviour, this);
    Paragraph.registerMutationHandler(behaviour, this);
  }

  protected void setHandler(AnnotationEvent.Handler handler) {
    this.handler = handler;
  }

  private static String getParagraphAnnotationValue(ContentElement node,
      ParagraphBehaviour behaviour) {
    if (ParagraphBehaviour.DEFAULT.equals(behaviour)) {
      return Alignment.fromValue(node.asElement().getAttribute(Paragraph.ALIGNMENT_ATTR))
          .cssValue();
    } else if (ParagraphBehaviour.HEADING.equals(behaviour)) {
      return node.asElement().getAttribute(Paragraph.SUBTYPE_ATTR);
    } else if (ParagraphBehaviour.LIST.equals(behaviour)) {
      return node.asElement().getAttribute(Paragraph.LIST_STYLE_ATTR);
    }

    return null;
  }

  @Override
  public void onAdded(ContentElement node) {
    if (handler != null && !AnnotationRegistry.muteHandlers) {
      String value = getParagraphAnnotationValue(node, behaviour);
      handler.exec(AnnotationEvent.build(AnnotationEvent.EVENT_DOM_ADDED,
          AnnotationValueBuilder.buildWithNode(key, value, node), null));
    }
  }

  @Override
  public void onMutation(ContentElement node) {
    if (handler != null && !AnnotationRegistry.muteHandlers) {
      String value = getParagraphAnnotationValue(node, behaviour);
      handler.exec(AnnotationEvent.build(AnnotationEvent.EVENT_DOM_MUTATED,
          AnnotationValueBuilder.buildWithNode(key, value, node), null));
    }
  }

  @Override
  public void onRemoved(ContentElement node) {
    if (handler != null && !AnnotationRegistry.muteHandlers) {
      String value = getParagraphAnnotationValue(node, behaviour);
      handler.exec(AnnotationEvent.build(AnnotationEvent.EVENT_DOM_REMOVED,
          AnnotationValueBuilder.buildWithNode(key, value, node), null));
    }
  }

  @Override
  public void onEvent(ContentElement node, Event event) {
    if (handler != null && !AnnotationRegistry.muteHandlers) {
      String value = getParagraphAnnotationValue(node, behaviour);
      handler.exec(AnnotationEvent.build(AnnotationEvent.EVENT_DOM_EVENT,
          AnnotationValueBuilder.buildWithNode(key, value, node), event));
    }
  }

}
