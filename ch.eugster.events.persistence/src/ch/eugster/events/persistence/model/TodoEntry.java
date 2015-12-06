package ch.eugster.events.persistence.model;

import java.util.Calendar;

public class TodoEntry {
	private AbstractEntity entity;

	private DueType dueType;

	public TodoEntry(AbstractEntity entity, DueType dueType) {
		this.entity = entity;
		this.dueType = dueType;
	}

	public String getEntityName() {
		return entity.getEntityName();
	}

	public String getInstanceName() {
		return entity.getInstanceName();
	}

	public String getDueType() {
		return this.dueType.getDueTypeName();
	}

	public Calendar getDueDate() {
		return this.dueType.getDueDate(entity);
	}

	public Calendar getCourseBeginTime() {
		if (this.entity instanceof Course) {
			Course course = (Course) entity;
			return course.getFirstDate();
		}
		return null;
	}

	public void setEntity(AbstractEntity entity)
	{
		this.entity = entity;
	}

	public AbstractEntity getEntity() {
		return this.entity;
	}

	public enum DueType {
		COURSE_ADVANCE_NOTICE_DATE, COURSE_INVITATION_DATE;

		public String getDueTypeName() {
			switch (this) {
			case COURSE_ADVANCE_NOTICE_DATE: {
				return ("Voranzeige verschicken");
			}
			case COURSE_INVITATION_DATE: {
				return "Einladung verschicken";
			}
			default: {
				return null;
			}
			}
		}

		public Calendar getDueDate(AbstractEntity entity) {
			switch (this) {
			case COURSE_ADVANCE_NOTICE_DATE: {
				if (entity instanceof Course) {
					return ((Course) entity).getAdvanceNoticeDate();
				}
				return null;
			}
			case COURSE_INVITATION_DATE: {
				if (entity instanceof Course) {
					return ((Course) entity).getInvitationDate();
				}
				return null;
			}
			default: {
				return null;
			}
			}
		}
	}
}
