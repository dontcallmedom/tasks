/*
 * Copyright (C) 2013 Marten Gajda <marten@dmfs.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package org.dmfs.tasks.widget;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.FieldDescriptor;
import org.dmfs.tasks.model.adapters.FieldAdapter;
import org.dmfs.tasks.model.adapters.TimeFieldAdapter;
import org.dmfs.tasks.model.adapters.TimeZoneWrapper;
import org.dmfs.tasks.model.layout.LayoutOptions;

import android.content.Context;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;


/**
 * Widget to display DateTime values
 * 
 * @author Arjun Naik <arjun@arjunnaik.in>
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class TimeFieldView extends AbstractFieldView
{
	/**
	 * {@link TimeZone} UTC, we use it when showing all-day dates.
	 */
	private final static TimeZone UTC = TimeZone.getTimeZone(Time.TIMEZONE_UTC);

	/**
	 * The {@link FieldAdapter} of the field for this view.
	 */
	private TimeFieldAdapter mAdapter;

	/**
	 * The text view that shows the time in the local time zone.
	 */
	private TextView mText;

	/**
	 * The text view that shows the time in the task's original time zone if it's different from the local time zone.
	 */
	private TextView mTimeZoneText;

	/**
	 * Formatters for date and time.
	 */
	private java.text.DateFormat mDefaultDateFormat, mDefaultTimeFormat;

	/**
	 * The default time zone on this device. Usually what the user has configured in the settings or what the provider returns.
	 */
	private TimeZone mDefaultTimeZone = new TimeZoneWrapper();


	public TimeFieldView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}


	public TimeFieldView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}


	public TimeFieldView(Context context)
	{
		super(context);
	}


	@Override
	protected void onFinishInflate()
	{
		super.onFinishInflate();
		mText = (TextView) findViewById(android.R.id.text1);
		mTimeZoneText = (TextView) findViewById(android.R.id.text2);
		mDefaultDateFormat = java.text.DateFormat.getDateInstance(SimpleDateFormat.LONG);
		mDefaultTimeFormat = DateFormat.getTimeFormat(getContext());
	}


	@Override
	public void setFieldDescription(FieldDescriptor descriptor, LayoutOptions layoutOptions)
	{
		super.setFieldDescription(descriptor, layoutOptions);
		mAdapter = (TimeFieldAdapter) descriptor.getFieldAdapter();
		mText.setHint(descriptor.getHint());
	}


	@Override
	public void onContentChanged(ContentSet contentSet)
	{
		Time newValue = mAdapter.get(mValues);
		if (mValues != null && newValue != null)
		{
			Date fullDate = new Date(newValue.toMillis(false));
			String formattedTime;
			if (!newValue.allDay)
			{
				mDefaultDateFormat.setTimeZone(mDefaultTimeZone);
				mDefaultTimeFormat.setTimeZone(mDefaultTimeZone);
				TimeZoneWrapper taskTimeZone = new TimeZoneWrapper(newValue.timezone);

				formattedTime = mDefaultDateFormat.format(fullDate) + " " + mDefaultTimeFormat.format(fullDate);

				if (!taskTimeZone.equals(mDefaultTimeZone) && mAdapter.hasTimeZoneField() && mTimeZoneText != null)
				{
					/*
					 * The date has a time zone that is different from the default time zone, so show the original time too.
					 */
					mDefaultDateFormat.setTimeZone(taskTimeZone);
					mDefaultTimeFormat.setTimeZone(taskTimeZone);

					mTimeZoneText.setText(mDefaultDateFormat.format(fullDate) + " " + mDefaultTimeFormat.format(fullDate) + " "
						+ taskTimeZone.getDisplayName(taskTimeZone.inDaylightTime(fullDate), TimeZone.SHORT));
					mTimeZoneText.setVisibility(View.VISIBLE);
				}
				else
				{
					mTimeZoneText.setVisibility(View.GONE);
				}
			}
			else
			{
				// all-day times are always in UTC
				mDefaultDateFormat.setTimeZone(UTC);
				formattedTime = mDefaultDateFormat.format(fullDate);
			}
			mText.setText(formattedTime);
			setVisibility(View.VISIBLE);
		}
		else
		{
			setVisibility(View.GONE);
		}
	}
}
