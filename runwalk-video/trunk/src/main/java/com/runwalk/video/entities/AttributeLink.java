package com.runwalk.video.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "ospos_attribute_links")
public class AttributeLink implements Serializable {

	@OneToOne
	@JoinColumn(name = "attribute_id")
	private AttributeValue attributeValue;

	@EmbeddedId
	private AttributeLinkId attributeLinkId;

	@ManyToOne
	@MapsId("definitionId") //This is the name of attr in EmployerDeliveryAgentPK class
	@JoinColumn(name = "definition_id")
	private AttributeDefinition attributeDefinition;

	@ManyToOne
	@MapsId("itemId")
	@JoinColumn(name = "item_id")
	private Item item;

	public AttributeDefinition getAttributeDefinition() {
		return attributeDefinition;
	}

	public Item getItem() {
		return item;
	}

	public AttributeValue getAttributeValue() {
		return attributeValue;
	}

	@Embeddable
	public static class AttributeLinkId implements Serializable {

		@Column(name = "definition_id")
		private Long definitionId;

		@Column(name="item_id")
		private Long itemId;

		public AttributeLinkId() {	}

		public AttributeLinkId(Item item, AttributeDefinition attributeDefinition) {
			this.itemId = item.getId();
			this.definitionId = attributeDefinition.getId();
		}

		public Long getDefinitionId() {
			return definitionId;
		}

		public Long getItemId() {
			return itemId;
		}

	}
		
}
