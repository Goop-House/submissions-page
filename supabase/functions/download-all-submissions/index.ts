// @ts-ignore
import { serve } from "https://deno.land/std@0.168.0/http/server.ts";
// @ts-ignore
import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

const corsHeaders = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Headers': 'authorization, x-client-info, apikey, content-type',
  'Access-Control-Allow-Methods': 'POST, OPTIONS',
};

declare global {
  namespace Deno {
    export const env: {
      get(key: string): string | undefined;
    };
  }
}

const supabaseUrl = Deno.env.get('SUPABASE_URL') ?? '';
const supabaseKey = Deno.env.get('SUPABASE_SERVICE_ROLE_KEY') ?? '';
const supabase = createClient(supabaseUrl, supabaseKey);

serve(async (req) => {
  if (req.method === 'OPTIONS') {
    return new Response(null, { headers: corsHeaders });
  }

  const authHeader = req.headers.get('authorization');
  if (!authHeader) {
    return new Response('Unauthorized', { status: 401, headers: corsHeaders });
  }

  const token = authHeader.replace('Bearer ', '');
  const { data: user, error } = await supabase.auth.getUser(token);

  if (error || !user) {
    return new Response('Unauthorized', { status: 401, headers: corsHeaders });
  }

  try {
    const { data: submissions, error } = await supabase
      .from('submissions')
      .select('*');

    if (error) throw error;

    const failedSubmissions: string[] = [];

    const submissionsWithUrls = await Promise.all(submissions.map(async (submission) => {
      try {
        const { data: audioUrl, error: audioError } = await supabase.storage
          .from('audio')
          .createSignedUrl(submission.audio_path, 3600); // 1 hour expiry

        if (audioError) {
          console.error(`Error creating signed URL for audio: ${audioError}`);
          throw new Error('Audio URL generation failed');
        }

        let artworkUrl = null;
        if (submission.art_path) {
          const { data: artUrl, error: artError } = await supabase.storage
            .from('artwork')
            .createSignedUrl(submission.art_path, 3600); // 1 hour expiry

          if (artError) {
            console.error(`Error creating signed URL for artwork: ${artError}`);
            throw new Error('Artwork URL generation failed');
          } else {
            artworkUrl = artUrl.signedUrl;
          }
        }

        return {
          ...submission,
          audioUrl: audioUrl.signedUrl,
          artworkUrl: artworkUrl
        };

      } catch (err) {
        failedSubmissions.push(submission.id);
        console.error(`Failed submission ID: ${submission.id}`, err);
        return null;
      }
    }));

    const successfulSubmissions = submissionsWithUrls.filter(Boolean);
    const jsonContent = JSON.stringify(successfulSubmissions);

    return new Response(jsonContent, {
      headers: {
        ...corsHeaders,
        "Content-Type": "application/json",
        "Content-Disposition": "attachment; filename=submissions.json"
      }
    });

  } catch (error) {
    console.error('Error:', error);
    return new Response(JSON.stringify({ error: error.message }), {
      status: 500,
      headers: { ...corsHeaders, "Content-Type": "application/json" }
    });
  }
});
