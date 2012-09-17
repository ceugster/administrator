package ch.eugster.events.persistence.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.eclipse.persistence.annotations.Customizer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;

@Entity
@Table(name = "events_address_type")
@AssociationOverrides({ @AssociationOverride(name = "user", joinColumns = @JoinColumn(name = "address_type_user_id")) })
@AttributeOverrides({ @AttributeOverride(name = "inserted", column = @Column(name = "address_type_inserted")),
		@AttributeOverride(name = "updated", column = @Column(name = "address_type_updated")),
		@AttributeOverride(name = "deleted", column = @Column(name = "address_type_deleted")),
		@AttributeOverride(name = "version", column = @Column(name = "address_type_version")) })
@Customizer(DeletedFilter.class)
public class AddressType extends AbstractEntity
{
	/**
	 * Data
	 */
	@Id
	@Column(name = "address_type_id")
	@GeneratedValue(generator = "events_address_type_id_seq")
	@TableGenerator(name = "events_address_type_id_seq", table = "events_sequence", allocationSize = 5)
	private Long id;

	@Basic
	@Column(name = "address_type_name")
	private String name;

	@Basic(fetch = FetchType.LAZY)
	@Lob
	@Column(name = "address_type_image_data", columnDefinition = "BLOB")
	private byte[] image;

	@Basic
	@Column(name = "address_type_image_type")
	private int imageType;

	// @Basic
	// @Column(name = "address_type_show_function")
	// @Convert("booleanConverter")
	// private boolean showFunction;
	//
	// @Basic
	// @Column(name = "address_type_show_contacts")
	// @Convert("booleanConverter")
	// private boolean showContacts;

	@Override
	public Long getId()
	{
		return id;
	}

	@Override
	public void setId(Long id)
	{
		this.id = id;
	}

	public String getName()
	{
		return stringValueOf(name);
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setImage(Image image, int imageType)
	{
		try
		{
			ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
			ImageLoader imageLoader = new ImageLoader();
			imageLoader.data = new ImageData[] { image.getImageData() };
			imageLoader.save(out, SWT.IMAGE_PNG);
			this.imageType = imageType;
			this.image = out.toByteArray();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public Image getImage()
	{
		Image image = null;
		try
		{
			if (this.image != null)
			{
				InputStream in = new ByteArrayInputStream(this.image);
				ImageData imageData = new ImageData(in);
				image = new Image(Display.getCurrent(), imageData);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return image;
	}

	public int getImageType()
	{
		return imageType;
	}

	public void setImageType(int imageType)
	{
		this.imageType = imageType;
	}

	// public void setShowFunction(boolean showFunction)
	// {
	// this.showFunction = showFunction;
	// }
	//
	// public boolean isShowFunction()
	// {
	// return showFunction;
	// }
	//
	// public void setShowContacts(boolean showContacts)
	// {
	// this.showContacts = showContacts;
	// }
	//
	// public boolean isShowContacts()
	// {
	// return showContacts;
	// }

	public static AddressType newInstance()
	{
		return (AddressType) AbstractEntity.newInstance(new AddressType());
	}

}
